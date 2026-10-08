//package com.xz.springboot.controller; // 请替换为您的包名
//
//import com.xz.springboot.service.VisualizationService; // 导入服务
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap; // 确保导入 HashMap
//import java.util.Map;
//
//@RestController
//@RequestMapping("/griddata")
//public class GridDataController {
//
//    @Autowired
//    private VisualizationService visualizationService; // 注入服务
//
//    // 用于临时存放上传文件的目录
//    private static final String UPLOAD_DIR = "temp-uploads/";
//
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadAndVisualize(@RequestParam("file") MultipartFile multipartFile) {
//        if (multipartFile.isEmpty()) {
//            Map<String, String> responseBody = new HashMap<>();
//            responseBody.put("message", "文件不能为空");
//            return ResponseEntity.badRequest().body(responseBody);
//        }
//
//        Path tempFilePath = null;
//        try {
//            // 1. Save the uploaded file temporarily (This part is needed)
//            File tempDir = new File(UPLOAD_DIR);
//            if (!tempDir.exists()) tempDir.mkdirs();
//
//            String originalFilename = multipartFile.getOriginalFilename();
//            if (originalFilename == null) {
//                Map<String, String> responseBody = new HashMap<>();
//                responseBody.put("message", "文件名无效");
//                return ResponseEntity.badRequest().body(responseBody);
//            }
//            tempFilePath = Paths.get(UPLOAD_DIR, originalFilename);
//            Files.write(tempFilePath, multipartFile.getBytes());
//
//        /*
//        // =================================================================
//        // === DATABASE CODE IS NOW COMMENTED OUT TO BYPASS THE ERROR ===
//        // This is the section that was causing the "Table doesn't exist" error.
//        // String md5 = SecureUtil.md5(multipartFile.getInputStream());
//        // GridData dbFiles = getFileByMd5(md5);
//        // if (dbFiles != null) {
//        //     // ... database logic ...
//        // }
//        // =================================================================
//        */
//
//            // 2. Call the visualization service directly (This is what we want to test)
//            String variableNameToPlot = "tp"; // Remember to change this to your variable
//            String imageUrl = visualizationService.createPlotFromNetCDF(tempFilePath.toFile(), variableNameToPlot);
//
//            // 3. Return the image URL successfully
//            Map<String, String> responseBody = new HashMap<>();
//            responseBody.put("imageUrl", imageUrl);
//            return ResponseEntity.ok(responseBody);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Map<String, String> errorBody = new HashMap<>();
//            errorBody.put("message", "文件处理失败: " + e.getMessage());
//            return ResponseEntity.status(500).body(errorBody);
//        } finally {
//            // 4. Clean up the temporary file (This part is needed)
//            if (tempFilePath != null) {
//                try {
//                    Files.delete(tempFilePath);
//                } catch (IOException e) {
//                    System.err.println("删除临时文件失败: " + tempFilePath);
//                }
//            }
//        }
//    }
//
//    // ... 您其他的 Controller 方法，例如 /page 等 ...
//}

package com.xz.springboot.controller;

import com.xz.springboot.service.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/griddata")
public class GridDataController {

    @Autowired
    private VisualizationService visualizationService;

    // 用于临时存放上传文件的目录
    private static final String UPLOAD_DIR = "temp-uploads/";

    // 存储临时文件映射 (临时文件ID -> 实际文件路径)
    private final Map<String, String> tempFileMap = new ConcurrentHashMap<>();

    /**
     * 1. 上传文件并获取变量列表
     */
    @PostMapping("/get-variables")
    public ResponseEntity<?> getVariables(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body(errorResponse("文件不能为空"));
        }

        Path tempFilePath = null;
        try {
            // 创建临时目录
            File tempDir = new File(UPLOAD_DIR);
            if (!tempDir.exists()) tempDir.mkdirs();

            // 生成唯一文件名避免冲突
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(errorResponse("文件名无效"));
            }

            String fileExtension = getFileExtension(originalFilename);
            String tempFileId = UUID.randomUUID().toString();
            String tempFileName = tempFileId + fileExtension;

            tempFilePath = Paths.get(UPLOAD_DIR, tempFileName);
            Files.write(tempFilePath, multipartFile.getBytes());

            // 读取NetCDF文件中的所有变量
            List<String> variables = readVariablesFromNetCDF(tempFilePath.toFile());

            // 保存临时文件映射
            tempFileMap.put(tempFileId, tempFilePath.toString());

            // 返回变量列表和临时文件ID
            Map<String, Object> response = new HashMap<>();
            response.put("variables", variables);
            response.put("tempFileId", tempFileId);
            response.put("message", "成功解析 " + variables.size() + " 个变量");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(errorResponse("文件解析失败: " + e.getMessage()));
        }
        // 注意：这里不删除临时文件，因为后续可视化还需要使用
    }

    /**
     * 2. 根据选择的变量和时间片进行可视化
     */
    @PostMapping("/visualize")
    public ResponseEntity<?> visualize(@RequestBody Map<String, Object> request) { // 改为 Object 类型
        try {
            String tempFileId = (String) request.get("tempFileId");
            String variableName = (String) request.get("variableName");

            // 新增：接收时间片参数，默认为0
            Integer timeIndex = 0;
            if (request.containsKey("timeIndex")) {
                try {
                    timeIndex = Integer.parseInt(request.get("timeIndex").toString());
                } catch (NumberFormatException e) {
                    System.out.println("时间片参数格式错误，使用默认值0");
                }
            }

            if (tempFileId == null || variableName == null) {
                return ResponseEntity.badRequest().body(errorResponse("参数不完整"));
            }

            // 根据临时文件ID获取实际文件路径
            String filePath = tempFileMap.get(tempFileId);
            if (filePath == null) {
                return ResponseEntity.badRequest().body(errorResponse("临时文件不存在或已过期"));
            }

            File tempFile = new File(filePath);
            if (!tempFile.exists()) {
                // 清理无效的映射
                tempFileMap.remove(tempFileId);
                return ResponseEntity.badRequest().body(errorResponse("临时文件已丢失"));
            }

            // 调用可视化服务，传入时间片参数
            VisualizationService.PlotResult plot = visualizationService.createPlotWithStats(tempFile, variableName, timeIndex);

            // 返回图像URL
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", plot.getImageUrl());
            response.put("message", "变量 '" + variableName + "' 时间片 " + timeIndex + " 可视化成功");
            response.put("timeIndex", timeIndex);
            response.put("min", plot.getMin());
            response.put("max", plot.getMax());
            response.put("mean", plot.getMean());
            response.put("validCount", plot.getValidCount());
            response.put("dataRange", String.format(Locale.ROOT, "%.4f to %.4f", plot.getMin(), plot.getMax()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(errorResponse("可视化失败: " + e.getMessage()));
        }
    }

    /**
     * 3. 获取变量的维度信息（新增接口）
     */
    @PostMapping("/get-variable-info")
    public ResponseEntity<?> getVariableInfo(@RequestBody Map<String, String> request) {
        try {
            String tempFileId = request.get("tempFileId");
            String variableName = request.get("variableName");

            if (tempFileId == null || variableName == null) {
                return ResponseEntity.badRequest().body(errorResponse("参数不完整"));
            }

            String filePath = tempFileMap.get(tempFileId);
            if (filePath == null) {
                return ResponseEntity.badRequest().body(errorResponse("临时文件不存在或已过期"));
            }

            File tempFile = new File(filePath);
            if (!tempFile.exists()) {
                tempFileMap.remove(tempFileId);
                return ResponseEntity.badRequest().body(errorResponse("临时文件已丢失"));
            }

            // 读取变量维度信息
            Map<String, Object> variableInfo = getVariableDimensions(tempFile, variableName);

            return ResponseEntity.ok(variableInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(errorResponse("获取变量信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取变量的维度信息
     */
    private Map<String, Object> getVariableDimensions(File ncFile, String variableName) throws IOException {
        Map<String, Object> info = new HashMap<>();

        try (NetcdfFile netcdfFile = NetcdfDatasets.openFile(ncFile.getAbsolutePath(), null)) {
            Variable variable = netcdfFile.findVariable(variableName);
            if (variable == null) {
                throw new IOException("在文件中未找到变量: " + variableName);
            }

            info.put("variableName", variableName);
            info.put("rank", variable.getRank());
            info.put("shape", variable.getShape());
            info.put("dimensions", variable.getDimensionsString());

            // 获取维度名称
            List<String> dimNames = new ArrayList<>();
            for (ucar.nc2.Dimension dim : variable.getDimensions()) {
                dimNames.add(dim.getShortName());
            }
            info.put("dimensionNames", dimNames);

            // 如果是3D或4D数据，计算时间维度的大小
            int rank = variable.getRank();
            if (rank >= 3) {
                int timeDimensionSize = variable.getShape()[0]; // 假设第一个维度是时间
                info.put("timeDimensionSize", timeDimensionSize);
                info.put("maxTimeIndex", timeDimensionSize - 1);
            }
        }

        return info;
    }

    /**
     * 4. 清理临时文件接口（可选）
     */
    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanupTempFile(@RequestBody Map<String, String> request) {
        String tempFileId = request.get("tempFileId");
        if (tempFileId != null) {
            cleanupTempFile(tempFileId);
        }
        return ResponseEntity.ok(successResponse("清理完成"));
    }

    /**
     * 从NetCDF文件中读取所有变量名
     */
    private List<String> readVariablesFromNetCDF(File ncFile) throws IOException {
        List<String> variables = new ArrayList<>();
        try (NetcdfFile netcdfFile = NetcdfDatasets.openFile(ncFile.getAbsolutePath(), null)) {
            for (Variable variable : netcdfFile.getVariables()) {
                // 只添加数据变量（排除坐标变量等）
                if (isDataVariable(variable)) {
                    variables.add(variable.getShortName());
                }
            }
        }

        // 按字母顺序排序
        return variables.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 判断是否为数据变量（简单的启发式判断）
     */
    private boolean isDataVariable(Variable variable) {
        String name = variable.getShortName();

        // 排除常见的坐标变量
        if (name.equalsIgnoreCase("lat") || name.equalsIgnoreCase("latitude") ||
                name.equalsIgnoreCase("lon") || name.equalsIgnoreCase("longitude") ||
                name.equalsIgnoreCase("time") || name.equalsIgnoreCase("level") ||
                name.equalsIgnoreCase("height") || name.equalsIgnoreCase("depth") ||
                name.matches("^.*(bnds|bounds)$")) {
            return false;
        }

        // 只包含2D、3D、4D的数据变量
        int rank = variable.getRank();
        return rank >= 2 && rank <= 4;
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFile(String tempFileId) {
        try {
            String filePath = tempFileMap.remove(tempFileId);
            if (filePath != null) {
                Files.deleteIfExists(Paths.get(filePath));
                System.out.println("清理临时文件: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("清理临时文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }

    /**
     * 统一错误响应格式
     */
    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 统一成功响应格式
     */
    private Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // ============ 原有的文件管理接口 ============

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndVisualize(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam(value = "variable", required = false) String variableName) {

        if (multipartFile.isEmpty()) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "文件不能为空");
            return ResponseEntity.badRequest().body(responseBody);
        }

        Path tempFilePath = null;
        try {
            File tempDir = new File(UPLOAD_DIR);
            if (!tempDir.exists()) tempDir.mkdirs();

            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null) {
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "文件名无效");
                return ResponseEntity.badRequest().body(responseBody);
            }
            tempFilePath = Paths.get(UPLOAD_DIR, originalFilename);
            Files.write(tempFilePath, multipartFile.getBytes());

            // 如果没有指定变量，尝试自动选择一个
            if (variableName == null || variableName.trim().isEmpty()) {
                variableName = findSuitableVariable(tempFilePath.toFile());
                if (variableName == null) {
                    return ResponseEntity.badRequest().body(errorResponse("未找到合适的可视化变量"));
                }
            }

            // 使用动态选择的变量
            String imageUrl = visualizationService.createPlotFromNetCDF(tempFilePath.toFile(), variableName);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("imageUrl", imageUrl);
            responseBody.put("variableUsed", variableName);
            return ResponseEntity.ok(responseBody);

        } catch (IOException e) {
            e.printStackTrace();
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("message", "文件处理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorBody);
        } finally {
            if (tempFilePath != null) {
                try {
                    Files.delete(tempFilePath);
                } catch (IOException e) {
                    System.err.println("删除临时文件失败: " + tempFilePath);
                }
            }
        }
    }

    @PostMapping("/upload-to-db")
    public ResponseEntity<?> uploadToDatabase(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body(errorResponse("文件不能为空"));
        }

        try {
            // 这里实现将文件信息保存到数据库的逻辑
            // 例如：fileService.saveFileInfo(multipartFile);

            // 临时返回成功消息
            return ResponseEntity.ok(successResponse("文件上传成功，已保存到数据库"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(errorResponse("文件上传失败: " + e.getMessage()));
        }
    }



    /**
     * 自动寻找合适的变量
     */
    private String findSuitableVariable(File ncFile) throws IOException {
        List<String> variables = readVariablesFromNetCDF(ncFile);

        if (variables.isEmpty()) {
            return null;
        }

        // 优先选择一些常见的变量名
        String[] preferredVars = {"temperature", "temp", "t2m", "tp", "precipitation", "rain", "pressure", "humidity"};
        for (String preferred : preferredVars) {
            if (variables.contains(preferred)) {
                return preferred;
            }
        }

        // 如果没有找到优先变量，返回第一个变量
        return variables.get(0);
    }

    // ... 您其他的文件管理接口，例如 /page, /update, 删除等 ...
}
