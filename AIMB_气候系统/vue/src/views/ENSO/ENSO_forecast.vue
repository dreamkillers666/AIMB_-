<template>
  <el-card class="dark-theme-card">
    <div class="sub-nav-container">
      <el-menu :default-active="$route.path" class="el-menu-dark-theme" mode="horizontal" router>
        <el-menu-item :index="sectionRoute($route.path, 'introduction')">ENSO介绍</el-menu-item>
        <el-menu-item :index="sectionRoute($route.path, 'results')">预测结果</el-menu-item>
        <el-menu-item :index="sectionRoute($route.path, 'data')">数据</el-menu-item>
        <el-menu-item :index="sectionRoute($route.path, 'resources')">更多资源</el-menu-item>
      </el-menu>
    </div>

    <div class="content-wrapper">
      <section class="content-section paper-header">
        <p class="paper-kicker">论文方法介绍 · Climate Dynamics (2026)</p>
        <h2 class="content-title">LSTA-Swin：长短期聚合时空神经网络</h2>
        <p class="main-text">
          LSTA-Swin 面向厄尔尼诺-南方涛动（ENSO）预测，使用 Swin Transformer 作为空间特征提取骨干，
          将完整历史序列中的年际趋势与最近季度的短期状态进行聚合，再通过编码器-解码器结构端到端预测未来海气异常场。
          该模型的目标是提高中长期 Niño3.4 / ONI 预测技巧，并减轻春季可预报性障碍（SPB）带来的影响。
        </p>
        <div class="paper-facts">
          <div><span>预测对象</span><strong>Niño3.4 / ONI</strong></div>
          <div><span>输入因子</span><strong>τx · τy · SSTA · SSTA150</strong></div>
          <div><span>时间窗口</span><strong>长期 12 个月 + 短期 3 个月</strong></div>
          <div><span>预测长度</span><strong>未来 20 个月</strong></div>
        </div>
      </section>

      <section class="content-section">
        <h2 class="content-title">数据与预测任务</h2>
        <p class="main-text">
          论文将 CMIP6、SODA 和 GODAS 的资料统一插值到 1° 网格，覆盖 90°E-30°W、30°S-30°N 的热带太平洋及周边区域。
          输入包括纬向风应力异常 τx、经向风应力异常 τy、海表温度异常 SSTA，以及 150 m 深度海表下温度异常 SSTA150。
          滑动窗口使用连续 32 个月样本，其中前 12 个月作为输入，后 20 个月作为预测目标；训练流程采用 CMIP6 预训练、SODA 微调、GODAS 评估的迁移学习方案。
        </p>
        <div class="two-column-layout method-grid">
          <div class="method-block">
            <h3 class="sub-heading">Niño3.4 指数与 ONI</h3>
            <p class="main-text small-text">
              Niño3.4 指数是 5°N-5°S、120°W-170°W 区域的海表温度异常平均值。论文使用该区域的 3 个月滑动平均计算 ONI；
              ONI 连续 5 个月高于 0.5°C 判定为厄尔尼诺，连续 5 个月低于 -0.5°C 判定为拉尼娜。
            </p>
          </div>
          <div class="method-block">
            <h3 class="sub-heading">模型结构</h3>
            <p class="main-text small-text">
              LST-IAM 提取长期与短期时间信息，Swin Block 通过窗口注意力与移位窗口建立多尺度空间联系，
              编码器-解码器恢复未来场，并用跨维度特征融合学习多变量海气耦合变化。
            </p>
          </div>
        </div>
      </section>

      <section class="content-section">
        <h2 class="content-title">预测技巧与事件回放</h2>
        <p class="main-text">
          在 1984-2017 年 GODAS 评估中，LSTA-Swin 的 11 个月提前期 Niño3.4 / ONI 相关技巧高于 CanCM4、CCSM3 和 GFDLaer04，
          论文报告的平均相关技巧分别提升 7.57%、23% 和 13.63%，并且有效预测能力可延伸至 20 个月。
          下图展示 ONI 相关技巧对比和 2015-2016 超强厄尔尼诺事件回放。
        </p>
        <div class="two-column-layout figure-grid">
          <figure class="paper-figure">
            <img src="../../assets/LSTA-Swin/figure-2-correlation.png" alt="LSTA-Swin 与其他模型的 ONI 相关技巧对比" />
            <figcaption>论文 Fig. 2：不同模型随提前期变化的 ONI 相关技巧。</figcaption>
          </figure>
          <figure class="paper-figure">
            <img src="../../assets/LSTA-Swin/figure-3-oni-comparison.png" alt="LSTA-Swin 2015-2016 厄尔尼诺 ONI 回放" />
            <figcaption>论文 Fig. 3：不同起报月份下的预测 ONI 与观测值对比。</figcaption>
          </figure>
        </div>
      </section>

      <section class="content-section">
        <h2 class="content-title">物理一致性与可解释性</h2>
        <p class="main-text">
          论文以 2015-2016 年超强厄尔尼诺为例，展示 LSTA-Swin 如何从风应力、海表温度和次表层温度的联合变化中重建“充电-释放-衰减”过程。
          2015 年 3 月至 8 月为充电阶段，西太平洋暖水和 SSTA150 增强；2015 年 10 月至 2016 年 1 月为释放阶段，暖水向东传播并推动 Niño3.4 区域增温；
          2016 年 2 月至 5 月为衰减阶段，异常逐渐扩散并减弱。显著性热图显示模型关注区域随 ENSO 演变从分散的前兆区域集中到 Niño3.4 核心区，再逐步扩散，体现出与已知海气物理过程一致的空间关注变化。
        </p>
        <div class="single-figure-row">
          <figure class="paper-figure wide-figure">
            <img src="../../assets/LSTA-Swin/figure-4-spatiotemporal.png" alt="LSTA-Swin 预测的 ENSO 时空演变" />
            <figcaption>论文 Fig. 4：2015 年 3 月至 2016 年 5 月的多变量时空预测结果。</figcaption>
          </figure>
          <figure class="paper-figure wide-figure">
            <img src="../../assets/LSTA-Swin/figure-5-saliency.png" alt="LSTA-Swin 预测相关显著性热图" />
            <figcaption>论文 Fig. 5：2015-2016 厄尔尼诺期间的预测相关显著性。</figcaption>
          </figure>
        </div>
      </section>

      <section class="content-section">
        <h2 class="content-title">消融实验与结论</h2>
        <p class="main-text">
          论文在 GODAS 数据上比较了不同结构变体的 20 个月平均相关技巧。原始 Swin Transformer 的相关技巧为 0.59，
          引入 LST-IAM 与 MLP 后为 0.65，完整 LSTA-Swin 达到 0.69；去除 MLP 或将 Swin Block 替换为普通 Self-Attention 后，技巧下降至约 0.62。
          结果说明长期-短期信息聚合、移位窗口空间建模和时空融合共同支撑了模型的中长期 ENSO 预测能力。
        </p>
        <div class="ablation-table-wrap">
          <table class="ablation-table">
            <thead><tr><th>模型变体</th><th>20个月平均 Corr</th><th>结果属性</th></tr></thead>
            <tbody>
              <tr><td>原始 Swin Transformer</td><td>0.59</td><td>论文消融结果</td></tr>
              <tr><td>LST-IAM + MLP</td><td>0.65</td><td>论文消融结果</td></tr>
              <tr class="highlight"><td>LSTA-Swin</td><td>0.69</td><td>论文消融结果</td></tr>
              <tr><td>去除 MLP</td><td>约 0.62</td><td>论文消融结果</td></tr>
              <tr><td>Self-Attention 替换 Swin Block</td><td>约 0.62</td><td>论文消融结果</td></tr>
            </tbody>
          </table>
        </div>
        <p class="reference-text">
          论文来源：Fang W., Zhang X.-Z., Li Y., Fu Z.-J., Fu H.-Y. “LSTA-Swin: a long- and short-term aggregated spatio-temporal neural network for enhanced ENSO forecasting.”
          Climate Dynamics, 2026, 64:269. DOI: 10.1007/s00382-026-08210-3。
        </p>
      </section>
    </div>
  </el-card>
</template>

<script>
import ensoSectionRoute from '@/utils/ensoSectionRoutes'

export default {
  name: 'enso_forecast',
  methods: { sectionRoute: ensoSectionRoute }
}
</script>

<style scoped>
.dark-theme-card {
  background-color: rgba(20, 30, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(100, 116, 139, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 16px;
}
::v-deep .el-card__body { padding: 24px 32px; }
.sub-nav-container { border-bottom: 1px solid rgba(100, 116, 139, 0.3); margin-bottom: 24px; }
.content-wrapper { line-height: 1.7; text-align: justify; }
.content-section { margin-top: 40px; padding-top: 40px; border-top: 1px solid rgba(100, 116, 139, 0.3); }
.content-section:first-child { margin-top: 0; padding-top: 0; border-top: none; }
.content-title { color: #58a6ff; margin-bottom: 20px; }
.paper-kicker { color: #67e8f9; font-size: 13px; letter-spacing: .03em; margin: 0 0 8px; }
.main-text { display: block; font-size: 16px; color: #e0e7ff; margin-bottom: 1em; }
.small-text { font-size: 14px; color: #cbd5e1; }
.paper-facts { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-top: 24px; }
.paper-facts > div { padding: 14px; border: 1px solid rgba(100, 116, 139, .35); background: rgba(0, 0, 0, .16); border-radius: 8px; }
.paper-facts span, .paper-facts strong { display: block; }
.paper-facts span { color: #94a3b8; font-size: 12px; margin-bottom: 6px; }
.paper-facts strong { color: #e6f7f4; font-size: 14px; }
.two-column-layout { display: flex; justify-content: space-between; gap: 24px; align-items: stretch; margin-top: 24px; }
.method-grid > .method-block { flex: 1; padding: 18px; border-left: 3px solid #58a6ff; background: rgba(0, 0, 0, .14); }
.sub-heading { color: #67e8f9; font-size: 17px; margin: 0 0 10px; }
.method-block .main-text { margin-bottom: 0; }
.figure-grid .paper-figure { width: calc(50% - 12px); }
.paper-figure { margin: 0; padding: 12px; border: 1px solid rgba(100, 116, 139, .3); background: rgba(255, 255, 255, .96); border-radius: 8px; }
.paper-figure img { width: 100%; height: auto; display: block; }
.paper-figure figcaption { color: #475569; font-size: 12px; line-height: 1.5; text-align: left; margin-top: 8px; }
.single-figure-row { display: flex; flex-direction: column; gap: 22px; margin-top: 24px; }
.wide-figure { width: 100%; box-sizing: border-box; }
.ablation-table-wrap { overflow-x: auto; margin-top: 20px; }
.ablation-table { width: 100%; border-collapse: collapse; color: #dbeafe; font-size: 14px; background: rgba(0, 0, 0, .14); }
.ablation-table th, .ablation-table td { border: 1px solid rgba(100, 116, 139, .35); padding: 11px 14px; text-align: left; }
.ablation-table th { color: #67e8f9; background: rgba(88, 166, 255, .1); font-weight: 600; }
.ablation-table .highlight td { color: #fff3c4; background: rgba(228, 183, 92, .13); }
.reference-text { display: block; background-color: rgba(0, 0, 0, .2); border-left: 3px solid #58a6ff; padding: 12px 16px; font-size: 13px; color: #94a3b8; border-radius: 4px; margin: 1.5em 0 0; }
::v-deep .el-menu.el-menu-dark-theme { background-color: transparent !important; border-bottom: none !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item { color: #cbd5e1 !important; background-color: transparent !important; border-bottom-color: transparent !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item:hover { background-color: rgba(88, 166, 255, .1) !important; color: #fff !important; }
::v-deep .el-menu.el-menu-dark-theme .el-menu-item.is-active { color: #67e8f9 !important; border-bottom: 2px solid #67e8f9 !important; }
@media (max-width: 800px) {
  ::v-deep .el-card__body { padding: 18px; }
  .paper-facts { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .two-column-layout { flex-direction: column; }
  .figure-grid .paper-figure { width: 100%; }
}
@media (max-width: 480px) {
  .paper-facts { grid-template-columns: 1fr; }
  .main-text { font-size: 15px; }
}
</style>
