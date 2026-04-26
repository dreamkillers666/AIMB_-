import torch
import torch.nn as nn

class ConvLSTMCell(nn.Module):
    def __init__(self, input_dim, hidden_dim, kernel_size, bias):
        super(ConvLSTMCell, self).__init__()
        self.input_dim = input_dim
        self.hidden_dim = hidden_dim
        self.kernel_size = kernel_size
        self.padding = kernel_size[0] // 2, kernel_size[1] // 2
        self.bias = bias
        self.conv = nn.Conv2d(in_channels=self.input_dim + self.hidden_dim,
                              out_channels=4 * self.hidden_dim,
                              kernel_size=self.kernel_size,
                              padding=self.padding,
                              bias=self.bias)

    def forward(self, input_tensor, cur_state):
        h_cur, c_cur = cur_state
        combined = torch.cat([input_tensor, h_cur], dim=1)
        combined_conv = self.conv(combined)
        cc_i, cc_f, cc_o, cc_g = torch.split(combined_conv, self.hidden_dim, dim=1)
        i = torch.sigmoid(cc_i)
        f = torch.sigmoid(cc_f)
        o = torch.sigmoid(cc_o)
        g = torch.tanh(cc_g)
        c_next = f * c_cur + i * g
        h_next = o * torch.tanh(c_next)
        return h_next, c_next

    def init_hidden(self, batch_size, image_size):
        height, width = image_size
        return (torch.zeros(batch_size, self.hidden_dim, height, width, device=self.conv.weight.device),
                torch.zeros(batch_size, self.hidden_dim, height, width, device=self.conv.weight.device))
class ConvLSTM(nn.Module):
    def __init__(self, input_dim, hidden_dim, kernel_size, num_layers,
                 batch_first=False, bias=True, return_all_layers=False):
        super(ConvLSTM, self).__init__()
        self._check_kernel_size_consistency(kernel_size)
        kernel_size = self._extend_for_multilayer(kernel_size, num_layers)
        hidden_dim = self._extend_for_multilayer(hidden_dim, num_layers)

        self.input_dim = input_dim
        self.hidden_dim = hidden_dim
        self.kernel_size = kernel_size
        self.num_layers = num_layers
        self.batch_first = batch_first
        self.bias = bias
        self.return_all_layers = return_all_layers

        cell_list = []
        for i in range(0, self.num_layers):
            cur_input_dim = self.input_dim if i == 0 else self.hidden_dim[i - 1]
            cell_list.append(ConvLSTMCell(input_dim=cur_input_dim,
                                          hidden_dim=self.hidden_dim[i],
                                          kernel_size=self.kernel_size[i],
                                          bias=self.bias))
        self.cell_list = nn.ModuleList(cell_list)

    def forward(self, input_tensor, hidden_state=None):
        if not self.batch_first:
            input_tensor = input_tensor.permute(1, 0, 2, 3, 4)
        b, _, _, h, w = input_tensor.size()
        if hidden_state is None:
            hidden_state = self._init_hidden(batch_size=b, image_size=(h, w))

        layer_output_list = []
        last_state_list = []
        seq_len = input_tensor.size(1)
        cur_layer_input = input_tensor

        for layer_idx in range(self.num_layers):
            h_state, c_state = hidden_state[layer_idx]
            output_inner = []
            for t in range(seq_len):
                h_state, c_state = self.cell_list[layer_idx](input_tensor=cur_layer_input[:, t, :, :, :],
                                                             cur_state=[h_state, c_state])
                output_inner.append(h_state)

            layer_output = torch.stack(output_inner, dim=1)
            cur_layer_input = layer_output
            layer_output_list.append(layer_output)
            last_state_list.append([h_state, c_state])

        if not self.return_all_layers:
            layer_output_list = layer_output_list[-1:]
            last_state_list = last_state_list[-1:]
        return layer_output_list, last_state_list

    def _init_hidden(self, batch_size, image_size):
        init_states = []
        for i in range(self.num_layers):
            init_states.append(self.cell_list[i].init_hidden(batch_size, image_size))
        return init_states

    @staticmethod
    def _check_kernel_size_consistency(kernel_size):
        if not (isinstance(kernel_size, tuple) or
                (isinstance(kernel_size, list) and all([isinstance(elem, tuple) for elem in kernel_size]))):
            raise ValueError('`kernel_size` must be tuple or list of tuples')

    @staticmethod
    def _extend_for_multilayer(param, num_layers):
        if not isinstance(param, list):
            param = [param] * num_layers
        return param
# ==========================================

class ENSO_Seq2Seq_ConvLSTM(nn.Module):
    """
    基于 Encoder-Decoder 架构的自回归 ConvLSTM 时空预测模型
    """

    def __init__(self, input_dim=3, hidden_dim=64, kernel_size=(3, 3), num_layers=1): # input_dim 默认改为 3
        super(ENSO_Seq2Seq_ConvLSTM, self).__init__()

        # 1. 编码器 (Encoder)：负责理解历史 3 个变量 (SSTA, U, V) 的演变规律
        self.encoder = ConvLSTM(input_dim=input_dim,  # 这里接收 3
                                hidden_dim=hidden_dim,
                                kernel_size=kernel_size,
                                num_layers=num_layers,
                                batch_first=True)

        # 2. 解码器 (Decoder)：负责一步步推演未来的 SSTA
        # 【关键修改】：解码器的输入永远是上一步预测出的 SSTA 图像，所以这里固定 input_dim=1
        self.decoder = ConvLSTM(input_dim=1,
                                hidden_dim=hidden_dim,
                                kernel_size=kernel_size,
                                num_layers=num_layers,
                                batch_first=True)

        # 3. 输出映射层：将 Decoder 特征降维成单通道 SSTA 图像，在最终输出前加入 20% 的 Dropout
        self.dropout = nn.Dropout2d(p=0.2)
        self.final_conv = nn.Conv2d(in_channels=hidden_dim, out_channels=1, kernel_size=1)

    def forward(self, x, predict_len=6):
        """
        :param x: 历史输入序列，Shape: (Batch, Lookback_Seq, Channel, Height, Width)
        :param predict_len: 需要预测的未来步数 (例如 6 个月)
        :return: 未来预测序列，Shape: (Batch, Predict_Len, Channel, Height, Width)
        """
        B, seq_len, C, H, W = x.size()

        # ====================
        # 第一阶段：编码 (Encoder Phase)
        # ====================
        # 我们只关心历史序列处理完后的最终隐状态 (last_state_list)
        _, last_state_list = self.encoder(x)

        # ====================
        # 第二阶段：解码与自回归预测 (Decoder Phase)
        # ====================
        outputs = []

        # 解码器的初始“记忆”就是编码器的最终“记忆”
        decoder_hidden = last_state_list

        # 【核心修复】：x 里面现在有 3 个变量 (SSTA, U, V)。
        # Decoder 只需要 SSTA（我们在 dataset 里把它拼在第 0 个通道），所以用 0:1 切片取出
        decoder_input = x[:, -1:, 0:1, :, :]  # Shape 变为: (Batch, 1, 1, H, W)

        for t in range(predict_len):
            decoder_out_seq, decoder_hidden = self.decoder(decoder_input, decoder_hidden)
            current_hidden = decoder_out_seq[0][:, 0, :, :, :]

            # 【新增】：在这里让特征随机失活
            current_hidden = self.dropout(current_hidden)

            step_pred = self.final_conv(current_hidden)

            # 收集结果
            outputs.append(step_pred)

            # 4. 【核心机制：自回归 Autoregressive】
            decoder_input = step_pred.unsqueeze(1)

        # 将 list 中每一阶段的预测图在时间维度拼起来
        # 最终 Shape: (Batch, Predict_Len, 1, H, W)
        final_output = torch.stack(outputs, dim=1)
        last_known_ssta = x[:, -1:, 0:1, :, :]
        final_output = final_output + last_known_ssta

        return final_output



# ==========================================
# 模型架构快速测试
# ==========================================
if __name__ == "__main__":
    # 模拟一个 Batch 的输入数据: Batch=16, 过去3个月, 单通道, 高度=20, 宽度=30
    dummy_x = torch.randn(16, 3, 1, 20, 30)

    # 初始化我们新的 Seq2Seq 模型
    model = ENSO_Seq2Seq_ConvLSTM(input_dim=1, hidden_dim=64, num_layers=1)

    # 我们先测试预测未来 6 个月
    predict_len = 6
    pred_y = model(dummy_x, predict_len=predict_len)

    print(f"输入历史数据维度: {dummy_x.shape}")
    print(f"输出预测数据维度: {pred_y.shape}")
    print("模型测试通过！")