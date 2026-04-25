package com.xz.springboot.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import com.xz.springboot.entity.EnsoData;
import com.xz.springboot.mapper.EnsoDataMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.xz.springboot.service.IEnsoDataService;
import com.xz.springboot.common.Result;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xz
 * @since 2024-11-18
 */
@RestController
@RequestMapping("/ensodata")
public class EnsoDataController {


    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Resource
    private EnsoDataMapper ensoDataMapper;
    @Resource
    private IEnsoDataService ensoDataService;

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody EnsoData ensoData) {
        return Result.success(ensoDataService.saveOrUpdate(ensoData));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(ensoDataService.list());
    }





    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();

        //先存储到磁盘
        // 定义一个文件唯一的标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;

        File uploadFile = new File(fileUploadPath + fileUUID);
        // 判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
        File parentFile = uploadFile.getParentFile();
        if(!parentFile.exists()) {
            parentFile.mkdirs();
        }

        String url;
        // 获取文件的md5
        String md5 = SecureUtil.md5(file.getInputStream());
        // 从数据库查询是否存在相同的记录
        EnsoData dbFiles = getFileByMd5(md5);
        if (dbFiles != null) { // 文件已存在
            url = dbFiles.getUrl();
        } else {
            // 上传文件到磁盘
            file.transferTo(uploadFile);
            // 数据库若不存在重复文件，则不删除刚才上传的文件
            url = "http://localhost:9090/ensodata/" + fileUUID;
        }

        // 存储数据库
        EnsoData saveFile = new EnsoData();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size/1024);
        saveFile.setUrl(url);
        saveFile.setMd5(md5);
        ensoDataMapper.insert(saveFile);
        return url;
    }




    /* 文件下载接口 http://localhost:9090/file/{fileUUID}
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        // 根据文件的唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    /**
     * 通过文件的md5查询文件
     * @param md5
     * @return
     */
    private EnsoData getFileByMd5(String md5) {
        // 查询文件的md5是否存在
        QueryWrapper<EnsoData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5", md5);
        List<EnsoData> filesList = ensoDataMapper.selectList(queryWrapper);
        return filesList.size() == 0 ? null : filesList.get(0);
    }

    @PostMapping("/update")
    public Result update(@RequestBody EnsoData files) {
        return Result.success(ensoDataMapper.updateById(files));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        EnsoData files = ensoDataMapper.selectById(id);
        files.setIsDelete(true);
        ensoDataMapper.updateById(files);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // select * from sys_file where id in (id,id,id...)
        QueryWrapper<EnsoData> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<EnsoData> files = ensoDataMapper.selectList(queryWrapper);
        for (EnsoData file : files) {
            file.setIsDelete(true);
            ensoDataMapper.updateById(file);
        }
        return Result.success();
    }



    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {
        QueryWrapper<EnsoData> queryWrapper = new QueryWrapper<>();
        // 查询未删除的记录
        queryWrapper.eq("is_delete", false);
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }
        return Result.success(ensoDataMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/detail/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(ensoDataMapper.selectById(id));
    }

}
