package MyForum.controller;

import MyForum.common.UserHolder;
import MyForum.service.CommonService;
import MyForum.util.CommonUtil;
import MyForum.util.MailClient;
import cn.hutool.core.util.StrUtil;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static MyForum.redis.RedisConstant.*;

/**
 * Date: 2022/8/11
 * Time: 11:08
 *
 * @Author SillyBaka
 *
 * Description：处理通用请求的Controller
 **/
@Controller
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private Producer producer;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private CommonService commonService;

    /**
     * 获取Kaptcha验证码和图片 并将图片作为响应返回
     * @param response 响应对象
     * @param session 会话对象
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletRequest request,HttpServletResponse response, HttpSession session){
        // 获取验证码和图片
        String verifyCode = producer.createText();
        BufferedImage image = producer.createImage(verifyCode);
////         将验证码存放进session域中
//        session.setAttribute("verifyCode",verifyCode);
        //todo 用redis代替session 存放验证码
        String sessionId = request.getRequestedSessionId();

        String key = LOGIN_CODE_KEY + sessionId;
        redisTemplate.opsForValue().set(key,verifyCode);
        // 设置验证码的过期时间
        redisTemplate.expire(key,LOGIN_CODE_EXPIRED_TIME,TimeUnit.SECONDS);

        // 通过响应对象的输出流 将图片写回小页面
        try {
            response.setContentType("image/png");
            ImageIO.write(image,"png",response.getOutputStream());
        } catch (IOException e) {
            log.error("获取验证码失败！",e);
        }
    }
    /**
     * 获取验证码
     * @param email 用户邮箱
     */
    @GetMapping("/forgetCode")
    public String getForgetCode(@RequestParam("email") String email, Model model){
        if(StrUtil.isBlank(email)){
            model.addAttribute("emailMsg","输入的邮箱不能为空！请重新输入");
            return "/site/forget";
        }
        // 获得验证码
        String verifyCode = producer.createText();
        String key = FORGET_CODE_KEY + email;
        // 将验证码存入redis中
        redisTemplate.opsForValue().set(key,verifyCode,FORGET_CODE_EXPIRED_TIME);

        // 将信息通过模板引擎封装成邮件
        Context context = new Context();
        context.setVariable("email",email);
        context.setVariable("verifyCode",verifyCode);

        String process = engine.process("/mail/forget", context);

        // 向目标邮箱发送验证码
        mailClient.sendMail(email,"重置密码邮件",process);

        model.addAttribute("verifyCodeMsg","已向该邮箱发送验证码！");

        return "/site/forget";
    }

    /**
     * 获取操作成功页面
     */
    @GetMapping("/success")
    public String toSuccessPage(){
        return "site/success";
    }


    /**
     * 点赞功能 抽象成单个控制器方法
     * @param entityType 实体类型
     * @param entityId 实体id
     */
    @GetMapping("/like/{entityType}/{entityId}")
    @ResponseBody
    public String like(@PathVariable("entityType") Integer entityType,
                       @PathVariable("entityId") Long entityId,
                       Long entityUserId){
        if(entityType == null || entityId == null || entityUserId == null){
            return CommonUtil.getJsonString(0,"点赞失败！参数不能为空");
        }
        Map<String, Object> result = commonService.like(entityType, entityId, entityUserId);

        return CommonUtil.getJsonString(200,"点赞成功",result);
    }

    /**
     * 关注指定用户
     * @param userId 指定用户id
     */
    @GetMapping("/follow/{userId}")
    @ResponseBody
    public String follow(@PathVariable("userId") Long userId){
        if(userId == null){
            return CommonUtil.getJsonString(0,"关注失败！参数不能为空");
        }
        if(userId.equals(UserHolder.getCurrentUser().getId())){
            return CommonUtil.getJsonString(0,"关注失败！不能关注自己喔");
        }

        Map<String, Object> result = commonService.follow(userId);

        return CommonUtil.getJsonString(200,"关注成功",result);
    }

    @GetMapping("/denied")
    public String denied(){
        return "site/error/404";
    }

    @GetMapping("/data")
    public String data(){
        return "site/admin/data";
    }
}
