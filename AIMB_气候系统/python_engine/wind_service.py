"""GFS 10m wind + SST data service -> leaflet-velocity JSON + Niño3.4 index.

Data sources (in order):
  1. AWS S3 mirror + HTTP Range requests (UGRD/VGRD 10m + TMP:surface, ~2.5MB)
  2. NOMADS Grib Filter (UGRD/VGRD 10m + TMP:surface subset, ~3.4MB)

Parsing: cfgrib (xarray engine). Downsampled to 1 deg grid (181x360).
Niño3.4: region mean (170W-120W / 190E-240E, 5S-5N) minus the 1981-2010
monthly climatology (NOAA OISST v2, sst.ltm.1981-2010.nc, cached locally).
Caching: in-memory + disk (wind_cache.json), 6h validity.
"""
import json
import os
import threading
import time
from datetime import datetime, timedelta, timezone

import numpy as np
import requests
import xarray as xr

AWS_BASE = "https://noaa-gfs-bdp-pds.s3.amazonaws.com"
NOMADS_FILTER = "https://nomads.ncep.noaa.gov/cgi-bin/filter_gfs_0p25.pl"

# NOAA OISST v2 1981-2010 monthly climatology (1 deg, 12 months, degC).
# Downloaded once to an ASCII temp path (netCDF4 can't open non-ASCII paths),
# then stored as a numpy .npz in the project dir for fast, path-safe loading.
CLIM_URL = "https://psl.noaa.gov/thredds/fileServer/Datasets/noaa.oisst.v2/sst.ltm.1981-2010.nc"
CLIM_NPZ = os.path.join(os.path.dirname(os.path.abspath(__file__)), "sst_climatology.npz")

CACHE_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "wind_cache.json")
CACHE_TTL = 6 * 3600  # 6 hours
CACHE_VERSION = 2     # bump to invalidate old caches

# leaflet-velocity grid header (1 deg global grid)
GRID = {
    "dx": 1, "dy": 1, "nx": 360, "ny": 181,
    "lo1": 0, "la1": 90, "lo2": 359, "la2": -90,
}

_lock = threading.Lock()
_memory_cache = None  # {"refTime": str, "fetched_at": float, "data": dict}
last_error = None     # human-readable error from the last failed refresh
_clim_cache = None    # cached xarray Dataset of the monthly climatology


def _utcnow():
    return datetime.now(timezone.utc)


def _cycle_candidates(now=None, max_tries=4):
    """Most recent GFS cycles (00/06/12/18 UTC), newest first.

    Handles "today 00z not out yet" by walking back to yesterday.
    """
    now = now or _utcnow()
    hour = (now.hour // 6) * 6
    base = now.replace(hour=hour, minute=0, second=0, microsecond=0)
    return [
        (base - timedelta(hours=6 * i)).strftime("%Y%m%d %H").split()
        for i in range(max_tries)
    ]


def _get_idx_lines(date, hour, timeout=30):
    """Download the GRIB .idx index file from AWS S3."""
    url = f"{AWS_BASE}/gfs.{date}/{hour}/atmos/gfs.t{hour}z.pgrb2.0p25.f000.idx"
    r = requests.get(url, timeout=timeout)
    r.raise_for_status()
    return r.text.splitlines()


def _find_ranges(lines, var, level):
    """Byte ranges [start, end] for var@level messages.

    end = start of the next record in the file - 1 (regardless of variable).
    """
    offsets = sorted(int(ln.split(":")[1]) for ln in lines if len(ln.split(":")) >= 2)
    starts = []
    for ln in lines:
        parts = ln.split(":")
        if len(parts) >= 5 and parts[3] == var and parts[4] == level:
            starts.append(int(parts[1]))
    ranges = []
    for s in starts:
        nxt = next((o for o in offsets if o > s), None)
        ranges.append((s, nxt - 1 if nxt is not None else None))
    return ranges


def _download_aws(date, hour, timeout=120):
    """AWS S3 + Range: download UGRD/VGRD 10m + TMP:surface messages (~2.5MB).

    AWS S3 does not support multipart/byteranges, so each range is a
    separate single-range request.
    """
    lines = _get_idx_lines(date, hour)
    ranges = _find_ranges(lines, "UGRD", "10 m above ground")
    ranges += _find_ranges(lines, "VGRD", "10 m above ground")
    ranges += _find_ranges(lines, "TMP", "surface")
    if not ranges:
        return None
    url = f"{AWS_BASE}/gfs.{date}/{hour}/atmos/gfs.t{hour}z.pgrb2.0p25.f000"
    chunks = []
    for s, e in ranges:
        r = requests.get(url, headers={"Range": f"bytes={s}-{e}"}, timeout=timeout)
        r.raise_for_status()
        chunks.append(r.content)
    return b"".join(chunks)


def _download_nomads(date, hour, timeout=180):
    """NOMADS Grib Filter: UGRD/VGRD 10m + TMP:surface subset (~3.4MB)."""
    params = {
        "file": f"gfs.t{hour}z.pgrb2.0p25.f000",
        "var_UGRD": "on",
        "var_VGRD": "on",
        "var_TMP": "on",
        "lev_10_m_above_ground": "on",
        "lev_surface": "on",
        "subregion": "",
        "leftlon": 0, "rightlon": 360, "toplat": 90, "bottomlat": -90,
        "dir": f"/gfs.{date}/{hour}/atmos",
    }
    r = requests.get(NOMADS_FILTER, params=params, timeout=timeout)
    r.raise_for_status()
    return r.content


def _parse_grib(raw):
    """cfgrib -> xarray Dataset. cfgrib needs a real file path (not BytesIO)."""
    import tempfile
    fd, path = tempfile.mkstemp(suffix=".grib2")
    try:
        with os.fdopen(fd, "wb") as f:
            f.write(raw)
        with xr.open_dataset(path, engine="cfgrib") as ds:
            return ds.load()
    finally:
        try:
            os.remove(path)
        except OSError:
            pass


def _extract_sst(ds):
    """TMP:surface -> 2D array in degC. cfgrib names it 't' (units K)."""
    for name in ("t", "sst", "t2m"):
        if name in ds:
            arr = ds[name].values
            if ds[name].attrs.get("units") == "K":
                arr = arr - 273.15
            return arr
    raise KeyError("TMP:surface not found in parsed GRIB")


def _to_velocity_json(ds):
    """Downsample to 1 deg (every 4th point of the 0.25 deg grid) and build JSON.

    GFS 0.25 deg grid: lon 0 -> 359.75 (ascending), so every 4th point lands
    exactly on integer degrees. Latitude may be descending (AWS) or ascending
    (NOMADS filter); we normalize to descending so the flattened array is
    row-major from la1=90 down to la2=-90.
    """
    u = ds["u10"].values
    v = ds["v10"].values
    sst = _extract_sst(ds)
    if ds["latitude"].values[0] < ds["latitude"].values[-1]:
        u = u[::-1, :]
        v = v[::-1, :]
        sst = sst[::-1, :]
    u = u[::4, ::4]
    v = v[::4, ::4]
    sst = sst[::4, ::4]
    ref_time = ds["time"].values.astype("datetime64[s]").astype(str).replace("T", " ")
    header_u = dict(GRID, parameterCategory=2, parameterNumber=2, refTime=ref_time)
    header_v = dict(GRID, parameterCategory=2, parameterNumber=3, refTime=ref_time)
    header_sst = dict(GRID, parameterCategory=0, parameterNumber=0, refTime=ref_time)
    return {
        "u": {"header": header_u, "data": u.astype(float).round(3).flatten().tolist()},
        "v": {"header": header_v, "data": v.astype(float).round(3).flatten().tolist()},
        "sst": {"header": header_sst, "data": sst.astype(float).round(3).flatten().tolist()},
        "nino34": _compute_nino34(sst, ref_time),
    }


# --- Niño3.4 index ---------------------------------------------------------

def _nino34_region_mean(sst2d, lat, lon):
    """Area-mean SST over Niño3.4 (190E-240E, 5S-5N) on a 1 deg grid."""
    lat_mask = (lat >= -5) & (lat <= 5)
    lon_mask = (lon >= 190) & (lon <= 240)
    region = sst2d[np.ix_(lat_mask, lon_mask)]
    return float(np.nanmean(region))


def _load_climatology(timeout=300):
    """Load the 1981-2010 monthly SST climatology.

    Returns {"sst": (12,180,360) degC, "lat": (180,), "lon": (360,)}.
    The raw .nc is downloaded once to an ASCII temp path (netCDF4 cannot open
    non-ASCII paths) and cached as a numpy .npz in the project dir.
    Raises on any failure so callers can fall back to anomaly_available=False.
    """
    global _clim_cache
    if _clim_cache is not None:
        return _clim_cache
    if not os.path.exists(CLIM_NPZ):
        import tempfile
        tmp_nc = os.path.join(tempfile.gettempdir(), "sst_climatology_dl.nc")
        r = requests.get(CLIM_URL, timeout=timeout)
        r.raise_for_status()
        with open(tmp_nc, "wb") as f:
            f.write(r.content)
        try:
            with xr.open_dataset(tmp_nc) as ds:
                sst = ds["sst"].values
                lat = ds["lat"].values
                lon = ds["lon"].values
        finally:
            try:
                os.remove(tmp_nc)
            except OSError:
                pass
        np.savez(CLIM_NPZ, sst=sst, lat=lat, lon=lon)
    z = np.load(CLIM_NPZ)
    _clim_cache = {"sst": z["sst"], "lat": z["lat"], "lon": z["lon"]}
    return _clim_cache


def _nino34_status(anomaly):
    if anomaly is None:
        return None
    if anomaly >= 0.5:
        return "El Niño"
    if anomaly <= -0.5:
        return "La Niña"
    return "Neutral"


def _compute_nino34(sst2d, ref_time):
    """Niño3.4 region mean + anomaly vs 1981-2010 climatology + status.

    sst2d is the downsampled 1 deg grid (lat descending 90..-90, lon 0..359).
    """
    lat = np.arange(90, -91, -1)
    lon = np.arange(0, 360)
    sst_val = _nino34_region_mean(sst2d, lat, lon)

    anomaly = None
    clim_month = None
    try:
        month = int(ref_time[5:7]) - 1  # 0-based month of the analysis time
        clim = _load_climatology()
        clim_val = _nino34_region_mean(clim["sst"][month], clim["lat"], clim["lon"])
        anomaly = sst_val - clim_val
        clim_month = month + 1
    except Exception as e:
        last_error = f"climatology: {e}"

    return {
        "sst": round(sst_val, 3),
        "anomaly": round(anomaly, 3) if anomaly is not None else None,
        "status": _nino34_status(anomaly),
        "climatology_month": clim_month,
        "anomaly_available": anomaly is not None,
    }


def _load_disk_cache():
    try:
        with open(CACHE_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    except (OSError, ValueError):
        return None


def _save_disk_cache(cache):
    try:
        with open(CACHE_FILE, "w", encoding="utf-8") as f:
            json.dump(cache, f)
    except OSError:
        pass


def _cache_valid(cache):
    if not cache:
        return False
    if cache.get("version") != CACHE_VERSION:
        return False
    if "sst" not in (cache.get("data") or {}):
        return False
    return time.time() - cache.get("fetched_at", 0) < CACHE_TTL


def get_latest_wind():
    """Return leaflet-velocity JSON for the latest available GFS cycle.

    Cache (memory + disk, 6h TTL) is returned directly when valid.
    Otherwise tries to refresh from AWS S3 (Range) then NOMADS (Grib Filter),
    walking back up to 4 cycles. Returns None on any failure (no exception).
    """
    global _memory_cache, last_error

    with _lock:
        mem = _memory_cache
        if mem is not None and _cache_valid(mem):
            return mem["data"]
        disk = _load_disk_cache()
        if disk is not None and _cache_valid(disk):
            _memory_cache = disk
            return disk["data"]

    for date, hour in _cycle_candidates():
        raw = None
        try:
            raw = _download_aws(date, hour)
        except Exception as e:
            last_error = f"AWS {date}/{hour}: {e}"
        if raw is None:
            try:
                raw = _download_nomads(date, hour)
            except Exception as e:
                last_error = f"NOMADS {date}/{hour}: {e}"
        if raw is None:
            continue
        try:
            ds = _parse_grib(raw)
            result = _to_velocity_json(ds)
        except Exception as e:
            last_error = f"parse {date}/{hour}: {e}"
            continue
        cache = {
            "version": CACHE_VERSION,
            "refTime": result["u"]["header"]["refTime"],
            "fetched_at": time.time(),
            "data": result,
        }
        with _lock:
            _memory_cache = cache
        _save_disk_cache(cache)
        last_error = None
        return result

    return None