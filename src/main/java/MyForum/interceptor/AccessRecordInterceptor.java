package MyForum.interceptor;

import MyForum.redis.RedisConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Date: 2022/10/8
 * Time: 20:48
 *
 * @Author SillyBaka
 * Description：用于记录全站访问量的拦截器
 **/
@Component
public class AccessRecordInterceptor implements HandlerInterceptor {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    // 成功访问后再记录用户的ip
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LocalDate date = LocalDate.now();
        String dailyKey = RedisConstant.getCountUVDaily(date);

        // 记录进行该访问的用户ip
        redisTemplate.opsForHyperLogLog().add(dailyKey,
                request.getRemoteAddr());
    }
}
