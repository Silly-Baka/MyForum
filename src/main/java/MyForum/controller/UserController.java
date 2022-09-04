package MyForum.controller;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.mapper.UserMapper;
import MyForum.pojo.User;
import MyForum.service.UserService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

import static MyForum.util.MyForumConstant.USER_LOGOUT_SUCCESS;

/**
 * Date: 2022/8/9
 * Time: 18:19
 *
 * @Author SillyBaka
 * Description：用户信息控制器
 **/
@Controller
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    // 偷懒用mapper
    @Autowired
    private UserMapper userMapper;


    /**
     * 打开登陆页面
     */
    @GetMapping("/login")
    public String loginPage(){
        return "site/login";
    }

    @PostMapping("/login")
    public String login(String username, String password, String verifyCode, HttpSession session, Model model){
        Map<String, Object> map = userService.login(username, password, verifyCode, session);
        // 如果登陆成功 则重定向到首页
        if(map.containsKey("successMsg")){
            return "redirect:/index";
        }else {
        // 登陆失败 则返回原登陆页面 并显示错误信息
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("verifyCodeMsg",map.get("verifyCodeMsg"));
            return "site/login";
        }
    }

    /**
     * 进入注册页面
     * @return
     */
    @GetMapping("/register")
    public String registerPage(){
        return "site/register";
    }

    /**
     * 注册用户
     * @return
     */
    @PostMapping("/register")
    public String register(User user, String confirmPassword,Model model){
        Map<String, Object> map = userService.register(user,confirmPassword);

        model.addAttribute("usernameMsg",map.get("usernameMsg"));
        model.addAttribute("passwordMsg",map.get("passwordMsg"));
        model.addAttribute("confirmPasswordMsg",map.get("confirmPasswordMsg"));
        model.addAttribute("emailMsg",map.get("emailMsg"));
        model.addAttribute("success",map.get("success"));

        return "site/register";
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session){
    // 直接删redis？对数据库压力会不会过大？若是只删本地token，那token被人获取了也还能继续登录 或者恶意访问数据库 不安全
    // 所以这边同时删除redis和客户端的token 减少去redis中查询的次数 同时维护了安全性
        String token = (String) session.getAttribute("token");
        Integer result = userService.logout(token);
        if(result == USER_LOGOUT_SUCCESS){
            session.removeAttribute("token");
            log.debug("登出成功！");
        }else {
            log.debug("登出失败，尚未登录！");
        }
        return "redirect:/user/login";
    }

    /**
     * 打开忘记密码页面
     */
    @GetMapping("/forget")
    public String forgetPage(){
        return "site/forget";
    }

    @PostMapping("/forget")
    public String forget(@RequestParam("email") String email,
                         @RequestParam("verifyCode") String verifyCode,
                         @RequestParam("password") String password,
                         Model model){
        Map<String, Object> result = userService.forget(email,verifyCode,password);

        model.addAttribute("emailMsg",result.get("emailMsg"));
        model.addAttribute("verifyCodeMsg",result.get("verifyCodeMsg"));
        model.addAttribute("passwordMsg",result.get("passwordMsg"));
        if(result.containsKey("success")){
            return "redirect:/common/success";
        }
        return "site/forget";
    }

    /**
     * 获取账号设置页面
     */
    @GetMapping("/setting")
    public String settingPage(){
        return "site/setting";
    }

    /**
     * 上传头像 并更改
     */
    @PostMapping("/upload")
    public String uploadHeaderImage(@RequestPart("headerImage") MultipartFile headerImage,Model model,HttpSession session) throws IOException {
        if(headerImage.isEmpty()){
            model.addAttribute("error","尚未选择图片！操作失败");
            return "site/setting";
        }
        UserDTO user = UserHolder.getCurrentUser();
        Map<String, String> result = userService.uploadHeaderImage(user, headerImage, (String) session.getAttribute("token"));

        if(!result.containsKey("success")){
            model.addAttribute("error",result.get("error"));
        }
        return "site/setting";
    }

    /**
     * 获取头像图片 并响应回客户端
     */
    @GetMapping("/header/{filename}")
    public void getHeaderImage(@PathVariable("filename") String filename, HttpServletResponse response){
        if(StrUtil.isBlank(filename)){
            return;
        }
        userService.getHeaderImage(filename,response);
    }


    @PostMapping("/password/change")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model ){
        Map<String, Object> result = userService.changePassword(oldPassword, newPassword, confirmPassword);

        model.addAttribute("oldPasswordMsg",result.get("oldPasswordMsg"));
        model.addAttribute("newPasswordMsg",result.get("newPasswordMsg"));
        model.addAttribute("confirmPasswordMsg",result.get("confirmPasswordMsg"));

        if(result.containsKey("success")){
            return "redirect:/user/logout";
        }else {
            return "site/setting";
        }
    }

    /**
     * 获取指定用户的主页
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") Long userId, Model model){
        if(userId == null){
            return "";
        }
        // 这里偷懒直接用mapper获取用户信息
        User user = userMapper.selectUserById(userId);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);

        model.addAttribute("user",userDTO);

        return "site/profile";
    }
}
