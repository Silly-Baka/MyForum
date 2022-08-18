package MyForum.interceptor;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.pojo.User;
import lombok.extern.slf4j.Slf4j;
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
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO user = UserHolder.getCurrentUser();
        if(user == null){
            log.info("未登录，请求已拦截，拦截路径为：{}",request.getRequestURI());
            response.sendRedirect("/index");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 每次请求 执行完controller方法后 渲染视图完后 删除user信息 防止内存泄漏
        UserHolder.removeUser();
    }
}
