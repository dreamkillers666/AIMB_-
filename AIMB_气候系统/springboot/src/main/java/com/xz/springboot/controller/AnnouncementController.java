package com.xz.springboot.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xz.springboot.common.Result;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.xz.springboot.service.IAnnouncementService;
import com.xz.springboot.entity.Announcement;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xz
 * @since 2023-05-02
 */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Resource
    private IAnnouncementService announcementService;

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Announcement announcement) {
        return Result.success(announcementService.saveOrUpdate(announcement));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(announcementService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(announcementService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(announcementService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(announcementService.getById(id));
    }

    @GetMapping("/maxId")
    public Result findMaxId() {
        Integer maxId=1;
        List<Announcement> list = announcementService.list();
        for(Announcement a: list){
            if (a.getId()>maxId){
                maxId=a.getId();
            }
        }
        return Result.success(announcementService.getById(maxId));
    }
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                            @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String title) {
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        if (!"".equals(title)) {
            queryWrapper.like("title", title);
        }
        queryWrapper.orderByDesc("id");
        return Result.success(announcementService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /*导出接口*/
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Announcement> list = announcementService.list();
        // 通过工具类创建writer 写出到磁盘路径
//        ExcelWriter writer = ExcelUtil.getWriter(filesUploadPath + "/用户信息.xlsx");
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);
        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("公告信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }

    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 方式1：(推荐) 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Announcement> list = reader.readAll(Announcement.class);
        announcementService.saveBatch(list);
        return Result.success(true);
    }
}
