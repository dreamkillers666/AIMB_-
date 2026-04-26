import os
import time
import numpy as np
import pandas as pd
import xarray as xr
import torch
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
from sklearn.metrics import mean_squared_error
from scipy.stats import pearsonr
from torch.utils.data import DataLoader
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

# 导入你的模型和数据集处理模块
from model import ENSO_Seq2Seq_ConvLSTM
from enso_dataset import ENSOSpatialDataset

app = FastAPI(title="ConvLSTM预测模型")

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

def load_model(weight_path):
    model = ENSO_Seq2Seq_ConvLSTM(input_dim=3, hidden_dim=64, num_layers=1)
    if os.path.exists(weight_path):
        model.load_state_dict(torch.load(weight_path, map_location=device))
        model.to(device)
        model.eval()
        print(f"成功加载模型: {weight_path}")
    else:
        print(f"警告: 找不到权重文件 {weight_path}")
    return model

# 确保你的路径是对的
models = {
    3: load_model(r"new_lead3_best_enso_seq2seq_v2.pth"),
    6: load_model(r"lead6_best_enso_seq2seq_v2.pth"),
    12: load_model(r"lead12_best_enso_seq2seq_v2.pth")
}

class PredictRequest(BaseModel):
    file_path: str
    lead_time: int

@app.post("/predict")
async def do_dynamic_evaluation(request: PredictRequest):
    if request.lead_time not in models:
        raise HTTPException(status_code=400, detail="不支持的预测时长")

    try:
        # === 1. 动态加载测试集 ===
        LOOKBACK = 6
        PREDICT_LEN = request.lead_time # 假设长度和 lead_time 一致
        BATCH_SIZE = 16

        test_dataset = ENSOSpatialDataset(request.file_path, lookback=LOOKBACK, predict_len=PREDICT_LEN, mode='test')
        test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE, shuffle=False)
        test_dates = test_dataset.time_dates

        # === 2. 实时模型推理 ===
        all_preds, all_trues = [], []
        model = models[request.lead_time]
        with torch.no_grad():
            for batch_X, batch_Y in test_loader:
                batch_X = batch_X.to(device)
                preds = model(batch_X, predict_len=PREDICT_LEN)
                all_preds.append(preds.squeeze(2).cpu().numpy())
                all_trues.append(batch_Y.squeeze(2).numpy())

        test_predictions = np.vstack(all_preds)
        test_trues = np.vstack(all_trues)

        # 提取指定的月份
        show_lead_idx = request.lead_time - 1
        pred_spatial = test_predictions[:, show_lead_idx, :, :]
        true_spatial = test_trues[:, show_lead_idx, :, :]

        # === 3. 生成掩码并计算误差 ===
        ds = xr.open_dataset(request.file_path)
        lat_coord = 'latitude' if 'latitude' in ds.coords else 'lat'
        lon_coord = 'longitude' if 'longitude' in ds.coords else 'lon'
        ds_coarsened = ds.coarsen({lat_coord: 4, lon_coord: 4}, boundary='trim').mean()

        lats, lons = ds_coarsened[lat_coord].values, ds_coarsened[lon_coord].values
        lat_mask = (lats <= 5) & (lats >= -5)
        lon_mask = (lons >= 190) & (lons <= 240)
        nino34_mask = lat_mask[:, None] & lon_mask[None, :]
        if nino34_mask.shape != pred_spatial.shape[1:]:
            nino34_mask = nino34_mask.T

        pred_nino34_idx = pred_spatial[:, nino34_mask].mean(axis=1)
        true_nino34_idx = true_spatial[:, nino34_mask].mean(axis=1)

        rmse_idx = np.sqrt(mean_squared_error(true_nino34_idx, pred_nino34_idx))
        cc_idx, _ = pearsonr(true_nino34_idx, pred_nino34_idx)
        spatial_rmse = np.sqrt(np.mean((true_spatial - pred_spatial) ** 2))

        # === 4. 实时生成 4 张图表 ===
        save_dir = os.path.abspath("../generated-plots")
        os.makedirs(save_dir, exist_ok=True)
        unique_id = int(time.time())

        sample_idx = -10
        sample_date = pd.to_datetime(test_dates[LOOKBACK + sample_idx]) + pd.DateOffset(months=show_lead_idx)

        true_map, pred_map = true_spatial[sample_idx], pred_spatial[sample_idx]
        diff_map = pred_map - true_map

        cmap = 'RdBu_r'
        vmax = max(np.abs(true_map).max(), np.abs(pred_map).max())
        norm = mcolors.TwoSlopeNorm(vmin=-vmax, vcenter=0, vmax=vmax)
        err_vmax = max(np.abs(diff_map).max(), 0.01)
        err_norm = mcolors.TwoSlopeNorm(vmin=-err_vmax, vcenter=0, vmax=err_vmax)

        # 图1: True SSTA
        plt.figure(figsize=(8, 4))
        im1 = plt.imshow(true_map, cmap=cmap, norm=norm, origin='lower')
        plt.colorbar(im1, fraction=0.046, pad=0.04, label='SST Anomaly (°C)')
        plt.title(f"True SSTA ({sample_date.strftime('%Y-%m')})")
        true_name = f"rt_true_{request.lead_time}m_{unique_id}.png"
        plt.savefig(os.path.join(save_dir, true_name), bbox_inches='tight', dpi=120)
        plt.close()

        # 图2: Predicted SSTA
        plt.figure(figsize=(8, 4))
        im2 = plt.imshow(pred_map, cmap=cmap, norm=norm, origin='lower')
        plt.colorbar(im2, fraction=0.046, pad=0.04, label='SST Anomaly (°C)')
        plt.title(f"Predicted SSTA (Lead={request.lead_time} Months)")
        pred_name = f"rt_pred_{request.lead_time}m_{unique_id}.png"
        plt.savefig(os.path.join(save_dir, pred_name), bbox_inches='tight', dpi=120)
        plt.close()

        # 图3: Difference
        plt.figure(figsize=(8, 4))
        im3 = plt.imshow(diff_map, cmap='PiYG_r', norm=err_norm, origin='lower')
        plt.colorbar(im3, fraction=0.046, pad=0.04, label='Error (°C)')
        plt.title(f"Difference (Pred - True) - Spatial RMSE={spatial_rmse:.2f}")
        diff_name = f"rt_diff_{request.lead_time}m_{unique_id}.png"
        plt.savefig(os.path.join(save_dir, diff_name), bbox_inches='tight', dpi=120)
        plt.close()

        # 图4: Line Chart
        plt.figure(figsize=(12, 4))
        start_date_idx = LOOKBACK + show_lead_idx
        plot_dates = pd.to_datetime(test_dates[start_date_idx: start_date_idx + len(test_predictions)])
        plt.plot(plot_dates, true_nino34_idx, label='True Nino 3.4', color='black', linewidth=2)
        plt.plot(plot_dates, pred_nino34_idx, label=f'Prediction', color='red', linestyle='--', linewidth=2)
        errors = pred_nino34_idx - true_nino34_idx
        plt.bar(plot_dates, errors, width=15, alpha=0.5, color=np.where(errors > 0, 'coral', 'skyblue'))
        plt.axhline(0, color='gray', linestyle='-', alpha=0.5)
        plt.title(f"Nino 3.4 Index (CC={cc_idx:.3f}, RMSE={rmse_idx:.3f})")
        plt.legend(loc='upper left')
        plt.grid(True, alpha=0.3)
        line_name = f"rt_line_{request.lead_time}m_{unique_id}.png"
        plt.savefig(os.path.join(save_dir, line_name), bbox_inches='tight', dpi=120)
        plt.close()

        # === 5. 返回给 Java 后端 ===
        return {
            "lead_time": request.lead_time,
            "enso_index": round(float(pred_nino34_idx[-1]), 3),
            "true_url": f"/generated-plots/{true_name}",
            "pred_url": f"/generated-plots/{pred_name}",
            "diff_url": f"/generated-plots/{diff_name}",
            "line_url": f"/generated-plots/{line_name}"
        }

    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))