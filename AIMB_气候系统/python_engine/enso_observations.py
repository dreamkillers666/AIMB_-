"""ENSO 观测数据服务（供前端实时面板使用）

行为说明：
1. 不做定时全量下载，而是**先检查数据是否更新**：
   对三个公开指数文件带 If-None-Match / If-Modified-Since 发条件请求，
   上游返回 304 即表示未更新，直接复用缓存（不重复解析、不重复传输）。
2. 只有返回 200（确实更新）时才重新解析并刷新缓存。
3. 上游不可达时返回上一次缓存，并标记 stale=True。
4. 最小检查间隔（默认 10 分钟）：避免每个前端请求都去探测上游。

数据源（NOAA CPC 公开指数文件）：
- ONI:    /data/indices/oni.ascii.txt
- SOI:    /data/indices/soi（只取 STANDARDIZED 表）
- Weekly: /data/indices/wksst9120.for（Niño 1+2 / 3 / 3.4 / 4 区域距平）
"""
import json
import os
import re
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone

import requests

CACHE_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "enso_observations_cache.json")
CHECK_MIN_INTERVAL = 600  # 距上次检查不足 10 分钟时直接用缓存（只影响探测频率，不影响数据新鲜度判断）
CACHE_VERSION = 1

BASE = "https://www.cpc.ncep.noaa.gov/data/indices/"
SOURCES = {
    "oni": BASE + "oni.ascii.txt",
    "soi": BASE + "soi",
    "weekly": BASE + "wksst9120.for",
}

SEASON_MID_MONTH = {
    "DJF": 1, "JFM": 2, "FMA": 3, "MAM": 4, "AMJ": 5, "MJJ": 6,
    "JJA": 7, "JAS": 8, "ASO": 9, "SON": 10, "OND": 11, "NDJ": 12,
}

MONTH_NUM = {
    "JAN": "01", "FEB": "02", "MAR": "03", "APR": "04", "MAY": "05", "JUN": "06",
    "JUL": "07", "AUG": "08", "SEP": "09", "OCT": "10", "NOV": "11", "DEC": "12",
}

_HEADERS = {"User-Agent": "AIMB-ENSO/1.0 (observation relay)"}

_state = {
    "raw": {},
    "etag": {},
    "last_modified": {},
    "payload": None,
    "checked_at": 0.0,
    "lock": threading.Lock(),
}


def _utcnow_iso():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def _load_cache():
    try:
        with open(CACHE_FILE, "r", encoding="utf-8") as handle:
            data = json.load(handle)
        if data.get("version") != CACHE_VERSION:
            return
        _state["raw"] = data.get("raw") or {}
        _state["etag"] = data.get("etag") or {}
        _state["last_modified"] = data.get("last_modified") or {}
        _state["payload"] = data.get("payload")
        _state["checked_at"] = float(data.get("checked_at") or 0)
    except (OSError, ValueError):
        pass


def _save_cache():
    data = {
        "version": CACHE_VERSION,
        "raw": _state["raw"],
        "etag": _state["etag"],
        "last_modified": _state["last_modified"],
        "payload": _state["payload"],
        "checked_at": _state["checked_at"],
    }
    tmp = CACHE_FILE + ".tmp"
    try:
        with open(tmp, "w", encoding="utf-8") as handle:
            json.dump(data, handle, ensure_ascii=False)
        os.replace(tmp, CACHE_FILE)
    except OSError:
        pass


def _fetch_one(name):
    """条件请求单个源；返回 True 表示内容有更新。"""
    url = SOURCES[name]
    headers = dict(_HEADERS)
    if _state["etag"].get(name):
        headers["If-None-Match"] = _state["etag"][name]
    if _state["last_modified"].get(name):
        headers["If-Modified-Since"] = _state["last_modified"][name]
    response = requests.get(url, headers=headers, timeout=20)
    if response.status_code == 304:
        return False
    response.raise_for_status()
    text = response.text
    if text == _state["raw"].get(name):
        return False
    _state["raw"][name] = text
    _state["etag"][name] = response.headers.get("ETag", "")
    _state["last_modified"][name] = response.headers.get("Last-Modified", "")
    return True


def _parse_oni(text):
    rows = []
    for line in (text or "").splitlines()[1:]:
        parts = line.split()
        if len(parts) < 4:
            continue
        month = SEASON_MID_MONTH.get(parts[0])
        if not month:
            continue
        try:
            rows.append([f"{parts[1]}-{month:02d}", round(float(parts[3]), 2)])
        except ValueError:
            continue
    return rows


def _parse_soi(text):
    marker = (text or "").find("STANDARDIZED")
    source = text[marker:] if marker > -1 else text
    rows = []
    for line in (source or "").splitlines():
        match = re.match(r"^(\d{4})\s+(.*)$", line.strip())
        if not match:
            continue
        values = re.findall(r"-?\d+\.\d+", match.group(2))
        if len(values) < 12:
            continue
        for index, raw in enumerate(values[:12], start=1):
            value = float(raw)
            if value <= -999:
                continue
            rows.append([f"{match.group(1)}-{index:02d}", round(value, 2)])
    return rows


def _parse_weekly(text):
    latest = None
    for line in (text or "").splitlines():
        match = re.match(r"^\s*(\d{2})([A-Z]{3})(\d{4})\s+(.*)$", line)
        if not match:
            continue
        day, mon, year, rest = match.groups()
        nums = re.findall(r"-?\d+\.\d+", rest)
        if len(nums) < 8 or mon not in MONTH_NUM:
            continue
        latest = {
            "date": f"{year}-{MONTH_NUM[mon]}-{day}",
            "nino12": round(float(nums[1]), 2),
            "nino3": round(float(nums[3]), 2),
            "nino34": round(float(nums[5]), 2),
            "nino4": round(float(nums[7]), 2),
            "sst": {
                "nino12": round(float(nums[0]), 2),
                "nino3": round(float(nums[2]), 2),
                "nino34": round(float(nums[4]), 2),
                "nino4": round(float(nums[6]), 2),
            },
        }
    return latest


def _build_payload():
    oni = _parse_oni(_state["raw"].get("oni"))
    soi = _parse_soi(_state["raw"].get("soi"))
    weekly = _parse_weekly(_state["raw"].get("weekly"))
    if not oni:
        return None
    soi_map = {month: value for month, value in soi}
    oni_map = {month: value for month, value in oni}
    months = sorted(set(oni_map) | set(soi_map))
    series = [[m, oni_map.get(m), soi_map.get(m)] for m in months]
    oni_last = oni[-1]
    soi_last = soi[-1] if soi else [None, None]
    return {
        "updatedAt": (weekly or {}).get("date") or oni_last[0],
        "latestWeekly": weekly,
        "latestMonthly": {
            "oni": oni_last[1],
            "oniMonth": oni_last[0],
            "soi": soi_last[1],
            "soiMonth": soi_last[0],
        },
        "recentOni": oni[-12:],
        "recentSoi": soi[-12:],
        "seriesCount": len(series),
    }


def _refresh():
    """并行检查三个源是否有更新；返回 (是否有数据变化, 错误信息)。"""
    changed = False
    errors = []
    with ThreadPoolExecutor(max_workers=len(SOURCES)) as pool:
        futures = {pool.submit(_fetch_one, name): name for name in SOURCES}
        for future in as_completed(futures):
            name = futures[future]
            try:
                if future.result():
                    changed = True
            except Exception as exc:  # noqa: BLE001 - 单个源失败不影响其它源
                errors.append(f"{name}: {type(exc).__name__}")
    if changed or _state["payload"] is None:
        payload = _build_payload()
        if payload is not None:
            payload["stale"] = False
            payload["checkedAt"] = _utcnow_iso()
            _state["payload"] = payload
    if _state["payload"] is not None:
        _state["payload"]["stale"] = bool(errors) and not changed
    _state["checked_at"] = time.time()
    _save_cache()
    return changed, "; ".join(errors)


def get_observations():
    """对外入口：返回观测数据；必要时先检查上游是否更新。"""
    with _state["lock"]:
        if not _state["payload"]:
            _load_cache()
        fresh_enough = (time.time() - _state["checked_at"]) < CHECK_MIN_INTERVAL
        if _state["payload"] is not None and fresh_enough:
            return _state["payload"]
        _, error = _refresh()
        if _state["payload"] is None:
            raise RuntimeError(error or "ENSO 观测数据暂不可用")
        payload = dict(_state["payload"])
        if error:
            payload["stale"] = True
            payload["note"] = error
        return payload


# 进程启动时先把落盘缓存读进来（避免首个请求等待上游）
_load_cache()
