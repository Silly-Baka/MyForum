package MyForum.controller;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.pojo.User;
import MyForum.service.UserService;
import cn.hutool.core.img.Img;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

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
            return "redirect:index";
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
     * 获得当前登陆用户的头像
     */
    @GetMapping("/headerImg")
    public void getCurrentHeaderImg(HttpServletResponse response) throws IOException {
        UserDTO user = UserHolder.getCurrentUser();
        Img img;
        if(user == null){
            // 用户没有登陆 返回默认的头像
            img = Img.from(Paths.get("http://images.nowcoder.com/head/1t.png"));
        }else {
            img = Img.from(Paths.get(user.getHeaderUrl()));
        }
        response.setContentType("image/png");
        img.write(response.getOutputStream());
    }

//    @GetMapping("/headerImg/{userId}")
//    public void getHeaderImgByUserId(@PathVariable("userId") Long userId){
//
//    }
}
