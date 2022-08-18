package MyForum.controller;

import MyForum.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static MyForum.util.MyForumConstant.ACTIVATION_SUCCESS;

/**
 * Date: 2022/8/9
 * Time: 22:17
 *
 * @Author SillyBaka
 * Description：处理激活的控制器
 **/
@Slf4j
@Controller
public class ActivationController {

    @Autowired
    private UserService userService;

    @GetMapping("/activation/{userId}/{code}")
    public String activation(@PathVariable("userId") Long userId,
                             @PathVariable("code") String activationCode){
        Integer result = userService.activation(userId, activationCode);
        // 激活成功 返回成功页面
        if(result == ACTIVATION_SUCCESS){
            return "site/operate-result";
        }else {
        // 激活失败 返回失败页面
            return "";
        }

    }

}
