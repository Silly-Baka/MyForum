package MyForum.controller;

import MyForum.pojo.User;
import MyForum.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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
    public String forget(Map<String,Object> map){
        return "";
    }

}
