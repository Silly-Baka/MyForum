package MyForum.controller;

import MyForum.service.DataService;
import MyForum.util.CommonUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Date: 2022/10/8
 * Time: 20:24
 *
 * @Author SillyBaka
 * Description：用于网站管理员统计数据的控制器
 **/
@Controller
@RequestMapping("/data")
public class DataController {

    @Resource
    private DataService dataService;

    /**
     * 获得当日的日活跃用户数量 (仅管理员可访问)
     */
    @PostMapping("/dau")
    @ResponseBody
    public String getDAU(String startDateStr, String endDateStr){
        if(StrUtil.isBlank(startDateStr)){
            return CommonUtil.getJsonString(403,"参数错误！起始日期不能为空");
        }
        if(StrUtil.isBlank(endDateStr)){
            endDateStr = startDateStr;
        }
        // 将日期字符串转换为日期
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(startDateStr,timeFormatter);
        LocalDate endDate = LocalDate.parse(endDateStr,timeFormatter);

        if(startDate.isAfter(endDate)){
            return CommonUtil.getJsonString(403,"参数错误！起始日期不能晚于结束日期");
        }

        Long dauTotal = dataService.getDAU(startDate, endDate);

        return CommonUtil.getJsonString(200,"统计日活跃用户数成功",
                Collections.singletonMap("result",dauTotal));
    }

    @PostMapping("/uv")
    @ResponseBody
    public String getUV(String startDateStr, String endDateStr){
        if(StrUtil.isBlank(startDateStr)){
            return CommonUtil.getJsonString(403,"参数错误！起始日期不能为空");
        }
        if(StrUtil.isBlank(endDateStr)){
            endDateStr = startDateStr;
        }
        // 将日期字符串转换为日期
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(startDateStr,timeFormatter);
        LocalDate endDate = LocalDate.parse(endDateStr,timeFormatter);

        if(startDate.isAfter(endDate)){
            return CommonUtil.getJsonString(403,"参数错误！起始日期不能晚于结束日期");
        }

        Long uvTotal = dataService.getDAU(startDate, endDate);

        return CommonUtil.getJsonString(200,"统计日活跃用户数成功",
                Collections.singletonMap("result",uvTotal));

    }
}
