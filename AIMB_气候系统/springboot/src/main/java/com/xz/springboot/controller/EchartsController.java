package com.xz.springboot.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Quarter;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xz.springboot.common.Constants;
import com.xz.springboot.common.Result;
import com.xz.springboot.entity.Emp;
import com.xz.springboot.entity.Files;
import com.xz.springboot.entity.Model;
import com.xz.springboot.entity.User;
import com.xz.springboot.mapper.FileMapper;
import com.xz.springboot.service.IEmpService;
import com.xz.springboot.service.IModelService;
import com.xz.springboot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xz.springboot.service.IModelService;
import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/echarts")
public class EchartsController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IEmpService empService;

    @Resource
    private FileMapper fileMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private IModelService modelService;

    @GetMapping("/example")
    public Result get() {
        Map<String, Object> map = new HashMap<>();
        map.put("x", CollUtil.newArrayList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        map.put("y", CollUtil.newArrayList(150, 230, 224, 218, 135, 147, 260));
        return Result.success(map);
    }

    @GetMapping("/members")
    public Result members() {
        List<User> list = userService.list();
        int q1 = 0; // 第一季度
        int q2 = 0; // 第二季度
        int q3 = 0; // 第三季度
        int q4 = 0; // 第四季度
        for (User user : list) {
            Date createTime = user.getCreateTime();
            Quarter quarter = DateUtil.quarterEnum(createTime);
            switch (quarter) {
                case Q1: q1 += 1; break;
                case Q2: q2 += 1; break;
                case Q3: q3 += 1; break;
                case Q4: q4 += 1; break;
                default: break;
            }
        }
        return Result.success(CollUtil.newArrayList(q1, q2, q3, q4));
    }

    @GetMapping("/degreess")
    public Result degreess() {
        List<Emp>  list = empService.list();
        int q1 = 0; // 高中
        int q2 = 0; // 大专
        int q3 = 0; // 本科
        int q4 = 0; // 研究生
        for (Emp emp : list) {
            String degree = emp.getDegree();
            switch (degree) {
                case "高中": q1 += 1; break;
                case "大专": q2 += 1; break;
                case "本科": q3 += 1; break;
                case "研究生": q4 += 1; break;
                default: break;
            }
        }
        return Result.success(CollUtil.newArrayList(q1, q2, q3, q4));
    }

    @GetMapping("/depts")
    public Result depts() {
        List<Emp>  list = empService.list();
        int q1 = 0; // 行政部
        int q2 = 0; // 财务部
        int q3 = 0; // 销售部
        int q4 = 0; // 营运部
        for (Emp emp : list) {
            String dept = emp.getDept();
            switch (dept) {
                case "行政部": q1 += 1; break;
                case "财务部": q2 += 1; break;
                case "销售部": q3 += 1; break;
                case "营运部": q4 += 1; break;
                default: break;
            }
        }
        return Result.success(CollUtil.newArrayList(q1, q2, q3, q4));
    }


    @GetMapping("/addresss")
    public Result addresss() {
        List<User> list = userService.list();
        int q1 = 0; // 江苏
        int q2 = 0; // 浙江
        int q3 = 0; // 湖南
        int q4 = 0; // 河南
        for (User user : list) {
            String address = user.getAddress();
            // 截取地址的前两个字符作为省份
            String province = address.substring(0, 2);
            switch (province) {
                case "江苏":
                    q1 += 1;
                    break;
                case "浙江":
                    q2 += 1;
                    break;
                case "湖南":
                    q3 += 1;
                    break;
                case "河南":
                    q4 += 1;
                    break;
                default:
                    break;
            }
        }
        return Result.success(CollUtil.newArrayList(q1, q2, q3, q4));
    }

    @GetMapping("/file/front/all")
    public Result frontAll() {
        List<Files> files;
        files = fileMapper.selectList(null);
        return Result.success(files);
    }

    @GetMapping("/SINTEX_F")
    public Result SINTEX_F() {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("name", "SINTEX_F");
        List<Model> models = modelService.listByMap(conditionMap);

        List<Double> accuracies = new ArrayList<>();

        for (Model model : models) {
            for (int i = 1; i <= 20; i++) {
                double accuracy = 0.0; // Default value if attribute doesn't exist

                switch (i) {
                    case 1:
                        accuracy = model.getMon1();
                        break;
                    case 2:
                        accuracy = model.getMon2();
                        break;
                    case 3:
                        accuracy = model.getMon3();
                        break;
                    case 4:
                        accuracy = model.getMon4();
                        break;
                    case 5:
                        accuracy = model.getMon5();
                        break;
                    case 6:
                        accuracy = model.getMon6();
                        break;
                    case 7:
                        accuracy = model.getMon7();
                        break;
                    case 8:
                        accuracy = model.getMon8();
                        break;
                    case 9:
                        accuracy = model.getMon9();
                        break;
                    case 10:
                        accuracy = model.getMon10();
                        break;
                    case 11:
                        accuracy = model.getMon11();
                        break;
                    case 12:
                        accuracy = model.getMon12();
                        break;
                    case 13:
                        accuracy = model.getMon13();
                        break;
                    case 14:
                        accuracy = model.getMon14();
                        break;
                    case 15:
                        accuracy = model.getMon15();
                        break;
                    case 16:
                        accuracy = model.getMon16();
                        break;
                    case 17:
                        accuracy = model.getMon17();
                        break;
                    case 18:
                        accuracy = model.getMon18();
                        break;
                    case 19:
                        accuracy = model.getMon19();
                        break;
                    case 20:
                        accuracy = model.getMon20();
                        break;
                    default:
                        break;
                }
                accuracies.add(accuracy);
            }
        }
        return Result.success(accuracies);
    }

    @GetMapping("/CNN")
    public Result CNN() {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("name", "CNN");
        List<Model> models = modelService.listByMap(conditionMap);

        List<Double> accuracies = new ArrayList<>();

        for (Model model : models) {
            for (int i = 1; i <= 20; i++) {
                double accuracy = 0.0; // Default value if attribute doesn't exist

                switch (i) {
                    case 1:
                        accuracy = model.getMon1();
                        break;
                    case 2:
                        accuracy = model.getMon2();
                        break;
                    case 3:
                        accuracy = model.getMon3();
                        break;
                    case 4:
                        accuracy = model.getMon4();
                        break;
                    case 5:
                        accuracy = model.getMon5();
                        break;
                    case 6:
                        accuracy = model.getMon6();
                        break;
                    case 7:
                        accuracy = model.getMon7();
                        break;
                    case 8:
                        accuracy = model.getMon8();
                        break;
                    case 9:
                        accuracy = model.getMon9();
                        break;
                    case 10:
                        accuracy = model.getMon10();
                        break;
                    case 11:
                        accuracy = model.getMon11();
                        break;
                    case 12:
                        accuracy = model.getMon12();
                        break;
                    case 13:
                        accuracy = model.getMon13();
                        break;
                    case 14:
                        accuracy = model.getMon14();
                        break;
                    case 15:
                        accuracy = model.getMon15();
                        break;
                    case 16:
                        accuracy = model.getMon16();
                        break;
                    case 17:
                        accuracy = model.getMon17();
                        break;
                    case 18:
                        accuracy = model.getMon18();
                        break;
                    case 19:
                        accuracy = model.getMon19();
                        break;
                    case 20:
                        accuracy = model.getMon20();
                        break;
                    default:
                        break;
                }
                accuracies.add(accuracy);
            }
        }
        return Result.success(accuracies);
    }

    @GetMapping("/Our_model")
    public Result Our_model() {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("name", "Ours");
        List<Model> models = modelService.listByMap(conditionMap);

        List<Double> accuracies = new ArrayList<>();

        for (Model model : models) {
            for (int i = 1; i <= 20; i++) {
                double accuracy = 0.0; // Default value if attribute doesn't exist

                switch (i) {
                    case 1:
                        accuracy = model.getMon1();
                        break;
                    case 2:
                        accuracy = model.getMon2();
                        break;
                    case 3:
                        accuracy = model.getMon3();
                        break;
                    case 4:
                        accuracy = model.getMon4();
                        break;
                    case 5:
                        accuracy = model.getMon5();
                        break;
                    case 6:
                        accuracy = model.getMon6();
                        break;
                    case 7:
                        accuracy = model.getMon7();
                        break;
                    case 8:
                        accuracy = model.getMon8();
                        break;
                    case 9:
                        accuracy = model.getMon9();
                        break;
                    case 10:
                        accuracy = model.getMon10();
                        break;
                    case 11:
                        accuracy = model.getMon11();
                        break;
                    case 12:
                        accuracy = model.getMon12();
                        break;
                    case 13:
                        accuracy = model.getMon13();
                        break;
                    case 14:
                        accuracy = model.getMon14();
                        break;
                    case 15:
                        accuracy = model.getMon15();
                        break;
                    case 16:
                        accuracy = model.getMon16();
                        break;
                    case 17:
                        accuracy = model.getMon17();
                        break;
                    case 18:
                        accuracy = model.getMon18();
                        break;
                    case 19:
                        accuracy = model.getMon19();
                        break;
                    case 20:
                        accuracy = model.getMon20();
                        break;
                    default:
                        break;
                }
                accuracies.add(accuracy);
            }
        }
        return Result.success(accuracies);
    }

    @GetMapping("/Transformer")
    public Result Transformer() {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("name", "Transformer");
        List<Model> models = modelService.listByMap(conditionMap);

        List<Double> accuracies = new ArrayList<>();

        for (Model model : models) {
            for (int i = 1; i <= 20; i++) {
                double accuracy = 0.0; // Default value if attribute doesn't exist

                switch (i) {
                    case 1:
                        accuracy = model.getMon1();
                        break;
                    case 2:
                        accuracy = model.getMon2();
                        break;
                    case 3:
                        accuracy = model.getMon3();
                        break;
                    case 4:
                        accuracy = model.getMon4();
                        break;
                    case 5:
                        accuracy = model.getMon5();
                        break;
                    case 6:
                        accuracy = model.getMon6();
                        break;
                    case 7:
                        accuracy = model.getMon7();
                        break;
                    case 8:
                        accuracy = model.getMon8();
                        break;
                    case 9:
                        accuracy = model.getMon9();
                        break;
                    case 10:
                        accuracy = model.getMon10();
                        break;
                    case 11:
                        accuracy = model.getMon11();
                        break;
                    case 12:
                        accuracy = model.getMon12();
                        break;
                    case 13:
                        accuracy = model.getMon13();
                        break;
                    case 14:
                        accuracy = model.getMon14();
                        break;
                    case 15:
                        accuracy = model.getMon15();
                        break;
                    case 16:
                        accuracy = model.getMon16();
                        break;
                    case 17:
                        accuracy = model.getMon17();
                        break;
                    case 18:
                        accuracy = model.getMon18();
                        break;
                    case 19:
                        accuracy = model.getMon19();
                        break;
                    case 20:
                        accuracy = model.getMon20();
                        break;
                    default:
                        break;
                }
                accuracies.add(accuracy);
            }
        }
        return Result.success(accuracies);
    }


    @GetMapping("/GRU")
    public Result GRU() {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("name", "GRU");
        List<Model> models = modelService.listByMap(conditionMap);

        List<Double> accuracies = new ArrayList<>();

        for (Model model : models) {
            for (int i = 1; i <= 20; i++) {
                double accuracy = 0.0; // Default value if attribute doesn't exist

                switch (i) {
                    case 1:
                        accuracy = model.getMon1();
                        break;
                    case 2:
                        accuracy = model.getMon2();
                        break;
                    case 3:
                        accuracy = model.getMon3();
                        break;
                    case 4:
                        accuracy = model.getMon4();
                        break;
                    case 5:
                        accuracy = model.getMon5();
                        break;
                    case 6:
                        accuracy = model.getMon6();
                        break;
                    case 7:
                        accuracy = model.getMon7();
                        break;
                    case 8:
                        accuracy = model.getMon8();
                        break;
                    case 9:
                        accuracy = model.getMon9();
                        break;
                    case 10:
                        accuracy = model.getMon10();
                        break;
                    case 11:
                        accuracy = model.getMon11();
                        break;
                    case 12:
                        accuracy = model.getMon12();
                        break;
                    case 13:
                        accuracy = model.getMon13();
                        break;
                    case 14:
                        accuracy = model.getMon14();
                        break;
                    case 15:
                        accuracy = model.getMon15();
                        break;
                    case 16:
                        accuracy = model.getMon16();
                        break;
                    case 17:
                        accuracy = model.getMon17();
                        break;
                    case 18:
                        accuracy = model.getMon18();
                        break;
                    case 19:
                        accuracy = model.getMon19();
                        break;
                    case 20:
                        accuracy = model.getMon20();
                        break;
                    default:
                        break;
                }
                accuracies.add(accuracy);
            }
        }
        return Result.success(accuracies);
    }

    @GetMapping("/STANet")
    public Result STANet() {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("name", "STANet");
        List<Model> models = modelService.listByMap(conditionMap);

        List<Double> accuracies = new ArrayList<>();

        for (Model model : models) {
            for (int i = 1; i <= 20; i++) {
                double accuracy = 0.0; // Default value if attribute doesn't exist

                switch (i) {
                    case 1:
                        accuracy = model.getMon1();
                        break;
                    case 2:
                        accuracy = model.getMon2();
                        break;
                    case 3:
                        accuracy = model.getMon3();
                        break;
                    case 4:
                        accuracy = model.getMon4();
                        break;
                    case 5:
                        accuracy = model.getMon5();
                        break;
                    case 6:
                        accuracy = model.getMon6();
                        break;
                    case 7:
                        accuracy = model.getMon7();
                        break;
                    case 8:
                        accuracy = model.getMon8();
                        break;
                    case 9:
                        accuracy = model.getMon9();
                        break;
                    case 10:
                        accuracy = model.getMon10();
                        break;
                    case 11:
                        accuracy = model.getMon11();
                        break;
                    case 12:
                        accuracy = model.getMon12();
                        break;
                    case 13:
                        accuracy = model.getMon13();
                        break;
                    case 14:
                        accuracy = model.getMon14();
                        break;
                    case 15:
                        accuracy = model.getMon15();
                        break;
                    case 16:
                        accuracy = model.getMon16();
                        break;
                    case 17:
                        accuracy = model.getMon17();
                        break;
                    case 18:
                        accuracy = model.getMon18();
                        break;
                    case 19:
                        accuracy = model.getMon19();
                        break;
                    case 20:
                        accuracy = model.getMon20();
                        break;
                    default:
                        break;
                }
                accuracies.add(accuracy);
            }
        }
        return Result.success(accuracies);
    }


}