package MyForum.interceptor;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.pojo.User;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

import static MyForum.util.RedisConstant.LOGIN_TOKEN_EXPIRED_TIME;
import static MyForum.util.RedisConstant.LOGIN_TOKEN_KEY;

/**
 * Date: 2022/8/17
 * Time: 16:53
 *
 * @Author SillyBaka
 *
 * Description：更新token过期时间的拦截器 （活跃用户则更新）
 **/
// 注入IOC容器 方便获取redistemplate
@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //用session模拟 request.getHeader("authorization")
        // 从客户端的请求头中取出token
        String token = (String) request.getSession().getAttribute("token");
        // token是空的 说明还没登陆 直接放行 让请求进入登陆拦截器
        if(StrUtil.isBlank(token)){
            return true;
        }
        String key = LOGIN_TOKEN_KEY + token;
        // 只要有token就可以登陆 直接从redis中获取用户信息
        User user = (User) redisTemplate.opsForValue().get(key);
        // redis中查不到 检查线程内还有没有 有就删掉 保持数据一致性
        if(user == null){
            UserHolder.removeUser();
            return true;
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        UserHolder.addUser(userDTO);

        //放到请求域当中 方便前端使用
        request.setAttribute("currentUser",user);

        // 更新redis中token的过期时间
        redisTemplate.expire(key,LOGIN_TOKEN_EXPIRED_TIME, TimeUnit.MINUTES);

        return true;
    }
}
