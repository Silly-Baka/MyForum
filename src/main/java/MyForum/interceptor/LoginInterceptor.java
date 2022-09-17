package MyForum.interceptor;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Date: 2022/8/17
 * Time: 16:08
 *
 * @Author SillyBaka
 *
 * Description：登陆拦截器
 **/
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO user = UserHolder.getCurrentUser();

        if(user == null){
            log.info("未登录，请求已拦截，拦截路径为：{}",request.getRequestURI());
            response.sendRedirect("/user/login");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
