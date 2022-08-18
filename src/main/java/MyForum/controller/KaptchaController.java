package MyForum.controller;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static MyForum.util.MyForumConstant.LOGIN_CODE_EXPIRED_TIME;
import static MyForum.util.MyForumConstant.LOGIN_CODE_KEY;

/**
 * Date: 2022/8/11
 * Time: 11:08
 *
 * @Author SillyBaka
 *
 * Description：处理Kaptcha验证码的Controller
 **/
@Controller
@Slf4j
public class KaptchaController {

    @Autowired
    private Producer producer;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

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
        redisTemplate.expire(key,LOGIN_CODE_EXPIRED_TIME);

        // 通过响应对象的输出流 将图片写回小页面
        try {
            response.setContentType("image/png");
            ImageIO.write(image,"png",response.getOutputStream());
        } catch (IOException e) {
            log.error("获取验证码失败！",e);
        }
    }
}
