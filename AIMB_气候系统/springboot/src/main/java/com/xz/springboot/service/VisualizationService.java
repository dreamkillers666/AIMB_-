//package com.xz.springboot.service;
//
//import org.springframework.stereotype.Service;
//import ucar.ma2.Array;
//import ucar.nc2.NetcdfFile;
//import ucar.nc2.Variable;
//import ucar.nc2.dataset.NetcdfDatasets;
//
//import javax.imageio.ImageIO;
//import java.awt.Color;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Service
//public class VisualizationService {
//
//    private static final String PLOT_OUTPUT_DIR = "generated-plots/";
//
//    public String createPlotFromNetCDF(File ncFile, String variableName) throws IOException {
//        // ... (这部分代码保持不变) ...
//        File outputDir = new File(PLOT_OUTPUT_DIR);
//        if (!outputDir.exists()) {
//            outputDir.mkdirs();
//        }
//
//        try (NetcdfFile netcdfFile = NetcdfDatasets.openFile(ncFile.getAbsolutePath(), null)) {
//
//            Variable dataVar = netcdfFile.findVariable(variableName);
//            if (dataVar == null) {
//                throw new IOException("在文件中未找到变量: " + variableName);
//            }
//
//            Array data;
//            int rank = dataVar.getRank();
//
//            if (rank == 2) {
//                data = dataVar.read();
//            } else if (rank == 3) {
//                System.out.println("这是一个3D变量，正在提取第一个2D切片...");
//                data = dataVar.read().slice(0, 0);
//            } else {
//                throw new IOException("只支持二维或三维变量的可视化，当前变量维度为: " + rank);
//            }
//
//            int[] shape = data.getShape();
//            int height = shape[0];
//            int width = shape[1];
//
//            float min = Float.MAX_VALUE;
//            float max = Float.MIN_VALUE;
//            for (int i = 0; i < data.getSize(); i++) {
//                float val = data.getFloat(i);
//                if (Float.isNaN(val)) continue;
//                if (val < min) min = val;
//                if (val > max) max = val;
//            }
//
//            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    float val = data.getFloat(y * width + x);
//                    // 现在调用的是新的彩色映射方法
//                    Color color = getColorForValue(val, min, max);
//                    image.setRGB(x, y, color.getRGB());
//                }
//            }
//
//            String outputFilename = ncFile.getName().replaceAll("\\.nc$", ".png");
//            Path outputPath = Paths.get(PLOT_OUTPUT_DIR, outputFilename);
//            ImageIO.write(image, "png", outputPath.toFile());
//
//            return "/" + outputFilename;
//        }
//    }
//
//    // ======================= 这是被替换和升级的部分 =======================
//
//    /**
//     * 新的颜色映射方法，将一个数值映射到一个彩虹色谱 (蓝 -> 绿 -> 红)
//     * @param value 当前数据点的值
//     * @param min 数据集中的最小值
//     * @param max 数据集中的最大值
//     * @return 对应的彩色 Color 对象
//     */
//    private Color getColorForValue(float value, float min, float max) {
//        if (Float.isNaN(value)) {
//            return Color.BLACK; // 无效值依然是黑色
//        }
//
//        // 将当前值归一化到 0.0f 到 1.0f 的范围
//        float normalized = (value - min) / (max - min);
//
//        // 定义色谱的关键颜色点 (0.0=蓝, 0.5=绿, 1.0=红)
//        Color c1 = Color.BLUE;
//        Color c2 = Color.GREEN;
//        Color c3 = Color.RED;
//
//        if (normalized < 0.5f) {
//            // 在 蓝 -> 绿 之间进行插值
//            // 将 0.0-0.5 的范围重新映射到 0.0-1.0
//            float remapped = normalized * 2.0f;
//            return interpolate(c1, c2, remapped);
//        } else {
//            // 在 绿 -> 红 之间进行插值
//            // 将 0.5-1.0 的范围重新映射到 0.0-1.0
//            float remapped = (normalized - 0.5f) * 2.0f;
//            return interpolate(c2, c3, remapped);
//        }
//    }
//
//    /**
//     * 辅助方法：在两个颜色之间进行线性插值
//     * @param startColor 起始颜色
//     * @param endColor 结束颜色
//     * @param proportion 插值比例 (0.0 到 1.0)
//     * @return 插值后的颜色
//     */
//    private Color interpolate(Color startColor, Color endColor, float proportion) {
//        float[] start = startColor.getRGBColorComponents(null);
//        float[] end = endColor.getRGBColorComponents(null);
//
//        float r = start[0] + (end[0] - start[0]) * proportion;
//        float g = start[1] + (end[1] - start[1]) * proportion;
//        float b = start[2] + (end[2] - start[2]) * proportion;
//
//        // 保证颜色分量在 0-1 范围内
//        r = Math.max(0, Math.min(1, r));
//        g = Math.max(0, Math.min(1, g));
//        b = Math.max(0, Math.min(1, b));
//
//        return new Color(r, g, b);
//    }
//}

package com.xz.springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class VisualizationService {

    @Value("${app.visualization.output-dir:generated-plots/}")
    private String plotOutputDir;

    /**
     * 默认方法，使用时间片0
     */
    public String createPlotFromNetCDF(File ncFile, String variableName) throws IOException {
        return createPlotFromNetCDF(ncFile, variableName, 0);
    }

    /**
     * 支持时间片参数的可视化方法
     */
    public String createPlotFromNetCDF(File ncFile, String variableName, int timeIndex) throws IOException {
        return createPlotWithStats(ncFile, variableName, timeIndex).getImageUrl();
    }

    public PlotResult createPlotWithStats(File ncFile, String variableName, int timeIndex) throws IOException {
        // 创建输出目录
        File outputDir = new File(plotOutputDir);
        if (!outputDir.exists()) {
            Files.createDirectories(outputDir.toPath());
        }

        try (NetcdfFile netcdfFile = NetcdfDatasets.openFile(ncFile.getAbsolutePath(), null)) {

            // 查找变量
            Variable dataVar = netcdfFile.findVariable(variableName);
            if (dataVar == null) {
                throw new IOException("在文件中未找到变量: " + variableName);
            }

            // 读取数据数组，传入时间片参数
            Array data = readDataArray(dataVar, timeIndex);

            // 计算数据统计信息
            DataStats stats = calculateDataStats(data);

            // 创建图像
            BufferedImage image = createImageFromData(data, stats);

            // 生成输出文件名并保存
            String outputFilename = generateOutputFilename(ncFile.getName(), variableName, timeIndex);
            Path outputPath = Paths.get(plotOutputDir, outputFilename);
            ImageIO.write(image, "png", outputPath.toFile());

            return new PlotResult("/generated-plots/" + outputFilename, stats);

        } catch (Exception e) {
            throw new IOException("可视化处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 读取数据数组，处理不同维度的情况，支持时间片
     */
    private Array readDataArray(Variable dataVar, int timeIndex) throws IOException {
        int rank = dataVar.getRank();
        int[] shape = dataVar.getShape();

        if (timeIndex < 0) {
            throw new IOException("时间片索引不能为负数");
        }

        System.out.println("变量 '" + dataVar.getShortName() + "' 的维度: " + rank +
                ", 形状: " + java.util.Arrays.toString(shape) +
                ", 时间片: " + timeIndex);

        try {
            switch (rank) {
                case 2:
                    System.out.println("二维变量，直接读取");
                    return dataVar.read();
                case 3:
                    // 三维数据：时间、纬度、经度 - 取指定时间片
                    System.out.println("三维变量，提取时间片: " + timeIndex);
                    if (timeIndex >= shape[0]) {
                        throw new IOException("时间片索引 " + timeIndex + " 超出范围，最大时间片为 " + (shape[0] - 1));
                    }
                    // 更安全的范围检查
                    int[] origin3d = {timeIndex, 0, 0};
                    int[] size3d = {1, shape[1], shape[2]};
                    System.out.println("三维读取范围: origin=" + java.util.Arrays.toString(origin3d) +
                            ", size=" + java.util.Arrays.toString(size3d));
                    return dataVar.read(origin3d, size3d);
                case 4:
                    // 四维数据：时间、高度、纬度、经度 - 取指定时间和第一个高度
                    System.out.println("四维变量，提取时间片: " + timeIndex + "，高度层: 0");
                    if (timeIndex >= shape[0]) {
                        throw new IOException("时间片索引 " + timeIndex + " 超出范围，最大时间片为 " + (shape[0] - 1));
                    }
                    // 更安全的范围检查
                    int[] origin4d = {timeIndex, 0, 0, 0};
                    int[] size4d = {1, 1, shape[2], shape[3]};
                    System.out.println("四维读取范围: origin=" + java.util.Arrays.toString(origin4d) +
                            ", size=" + java.util.Arrays.toString(size4d));
                    return dataVar.read(origin4d, size4d);
                default:
                    throw new IOException("不支持的数据维度: " + rank + "，只支持2D、3D或4D数据");
            }
        } catch (ucar.ma2.InvalidRangeException e) {
            throw new IOException("数据范围无效: " + e.getMessage() +
                    ", 变量形状: " + java.util.Arrays.toString(shape), e);
        }
    }
    /**
     * 计算数据统计信息
     */
    private DataStats calculateDataStats(Array data) {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        int validCount = 0;
        double sum = 0;

        // 第一次遍历：计算最小、最大值
        for (int i = 0; i < data.getSize(); i++) {
            float val = data.getFloat(i);
            if (Float.isFinite(val)) {
                if (val < min) min = val;
                if (val > max) max = val;
                sum += val;
                validCount++;
            }
        }

        // 处理全为NaN的情况
        if (validCount == 0) {
            min = 0;
            max = 1;
        }

        return new DataStats(min, max, validCount > 0 ? (float) (sum / validCount) : Float.NaN, validCount);
    }

    /**
     * 从数据创建图像
     */
    private BufferedImage createImageFromData(Array data, DataStats stats) {
        int[] shape = data.getShape();
        int height, width;

        // 根据维度调整高度和宽度
        if (shape.length == 2) {
            height = shape[0];
            width = shape[1];
        } else if (shape.length == 3) {
            height = shape[1];
            width = shape[2];
        } else {
            height = shape[2];
            width = shape[3];
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 创建 Index 对象用于多维访问
        ucar.ma2.Index index = data.getIndex();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float val;

                if (shape.length == 2) {
                    // 二维数据：设置索引 [y, x]
                    val = data.getFloat(index.set(y, x));
                } else if (shape.length == 3) {
                    // 三维数据：设置索引 [0, y, x]
                    val = data.getFloat(index.set(0, y, x));
                } else {
                    // 四维数据：设置索引 [0, 0, y, x]
                    val = data.getFloat(index.set(0, 0, y, x));
                }

                Color color = getColorForValue(val, stats.min, stats.max);
                image.setRGB(x, y, color.getRGB());
            }
        }

        return image;
    }
    /**
     * 生成输出文件名（包含时间片信息）
     */
    private String generateOutputFilename(String originalFilename, String variableName, int timeIndex) {
        String baseName = originalFilename.replaceAll("\\.[^.]*$", ""); // 移除扩展名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s_t%d_%s_%s.png", baseName, variableName, timeIndex, timestamp, uuid);
    }

    /**
     * 颜色映射方法
     */
    private Color getColorForValue(float value, float min, float max) {
        if (!Float.isFinite(value)) {
            return Color.GRAY; // 使用灰色表示无效值
        }

        // 将当前值归一化到 0.0f 到 1.0f 的范围
        float normalized = max == min ? 0.5f : (value - min) / (max - min);
        normalized = Math.max(0, Math.min(1, normalized));

        // 定义色谱的关键颜色点 (0.0=蓝, 0.5=绿, 1.0=红)
        Color c1 = Color.BLUE;
        Color c2 = Color.GREEN;
        Color c3 = Color.RED;

        if (normalized < 0.5f) {
            // 在 蓝 -> 绿 之间进行插值
            float remapped = normalized * 2.0f;
            return interpolate(c1, c2, remapped);
        } else {
            // 在 绿 -> 红 之间进行插值
            float remapped = (normalized - 0.5f) * 2.0f;
            return interpolate(c2, c3, remapped);
        }
    }

    /**
     * 颜色插值方法
     */
    private Color interpolate(Color startColor, Color endColor, float proportion) {
        float[] start = startColor.getRGBColorComponents(null);
        float[] end = endColor.getRGBColorComponents(null);

        float r = start[0] + (end[0] - start[0]) * proportion;
        float g = start[1] + (end[1] - start[1]) * proportion;
        float b = start[2] + (end[2] - start[2]) * proportion;

        r = Math.max(0, Math.min(1, r));
        g = Math.max(0, Math.min(1, g));
        b = Math.max(0, Math.min(1, b));

        return new Color(r, g, b);
    }

    /**
     * 数据统计信息内部类
     */
    private static class DataStats {
        final float min;
        final float max;
        final float mean;
        final int validCount;

        DataStats(float min, float max, float mean, int validCount) {
            this.min = min;
            this.max = max;
            this.mean = mean;
            this.validCount = validCount;
        }
    }

    public static class PlotResult {
        private final String imageUrl;
        private final float min;
        private final float max;
        private final float mean;
        private final int validCount;

        PlotResult(String imageUrl, DataStats stats) {
            this.imageUrl = imageUrl;
            this.min = stats.min;
            this.max = stats.max;
            this.mean = stats.mean;
            this.validCount = stats.validCount;
        }

        public String getImageUrl() { return imageUrl; }
        public float getMin() { return min; }
        public float getMax() { return max; }
        public float getMean() { return mean; }
        public int getValidCount() { return validCount; }
    }
}
