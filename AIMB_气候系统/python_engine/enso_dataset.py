import xarray as xr
import numpy as np
import pandas as pd
import torch
from torch.utils.data import Dataset, DataLoader


class ENSOSpatialDataset(Dataset):
    def __init__(self, file_path, lookback=6, predict_len=6, mode='train', train_ratio=0.8):
        self.lookback = lookback
        self.predict_len = predict_len
        self.mode = mode

        # 1. 加载数据
        ds = xr.open_dataset(file_path)
        time_coord = 'valid_time' if 'valid_time' in ds.coords else 'time'

        lat_coord = 'latitude' if 'latitude' in ds.coords else 'lat'
        lon_coord = 'longitude' if 'longitude' in ds.coords else 'lon'
        ds = ds.coarsen({lat_coord: 4, lon_coord: 4}, boundary='trim').mean()

        total_months = len(ds[time_coord])
        split_idx = int(total_months * train_ratio)

        # 【核心新增】：封装一个计算距平的函数
        def get_anomaly(data_source, var_name):
            train_ds = data_source.isel({time_coord: slice(0, split_idx)})
            climatology = train_ds[var_name].groupby(f'{time_coord}.month').mean(dim=time_coord)
            anom = (data_source[var_name].groupby(f'{time_coord}.month') - climatology).values
            return np.nan_to_num(anom, nan=0.0)

        # 2. 获取三个变量的距平 (请确保 'u10' 和 'v10' 是你 .nc 文件里真实的变量名，如果不是请修改！)
        u_var = 'u10' if 'u10' in ds.data_vars else 'u' # 自动适配常见风场变量名
        v_var = 'v10' if 'v10' in ds.data_vars else 'v'

        ssta_raw = get_anomaly(ds, 'sst')
        ua_raw = get_anomaly(ds, u_var)
        va_raw = get_anomaly(ds, v_var)

        # 3. 根据 mode 划分数据段
        if mode == 'train':
            self.ssta = ssta_raw[:split_idx]
            self.ua = ua_raw[:split_idx]
            self.va = va_raw[:split_idx]
            self.time_dates = ds[time_coord].values[:split_idx]
        else:
            start_idx = split_idx - lookback
            self.ssta = ssta_raw[start_idx:]
            self.ua = ua_raw[start_idx:]
            self.va = va_raw[start_idx:]
            self.time_dates = ds[time_coord].values[start_idx:]

        self.num_samples = len(self.ssta) - self.lookback - self.predict_len + 1

    def __len__(self):
        return self.num_samples

    def __getitem__(self, idx):
        # 提取过去几个月的三变量数据
        X_sst = self.ssta[idx: idx + self.lookback]
        X_u = self.ua[idx: idx + self.lookback]
        X_v = self.va[idx: idx + self.lookback]

        # 【核心修改】：将三个变量沿着 Channel 维度 (axis=1) 拼接起来 -> Shape: (Seq, 3, H, W)
        X_seq = np.stack([X_sst, X_u, X_v], axis=1)

        # 输出 Y 依然只是未来的 SSTA (Seq, 1, H, W)
        Y_seq = self.ssta[idx + self.lookback: idx + self.lookback + self.predict_len]
        Y_seq = np.expand_dims(Y_seq, axis=1)

        return torch.tensor(X_seq, dtype=torch.float32), torch.tensor(Y_seq, dtype=torch.float32)


# ==========================================
# 测试数据管道的有效性 (你可以直接运行这一小段试试看)
# ==========================================
if __name__ == "__main__":
    test_file_path = r"D:\1_ENSO_prediction\code\era5download\stdmamba_data\surface_data\2997adb93a53e5435d0933fecbde29dc.nc"

    try:
        train_dataset = ENSOSpatialDataset(test_file_path, lookback=3, predict_len=6, mode='train')
        train_loader = DataLoader(train_dataset, batch_size=16, shuffle=True)

        X_batch, Y_batch = next(iter(train_loader))
        print(f"数据管道测试成功！")
        print(f"X_batch shape: {X_batch.shape} # 预期: (16, 3, 1, H, W)")
        print(f"Y_batch shape: {Y_batch.shape} # 预期: (16, 6, 1, H, W)")
    except Exception as e:
        print(f"加载失败，请检查路径或 xarray 变量名: {e}")