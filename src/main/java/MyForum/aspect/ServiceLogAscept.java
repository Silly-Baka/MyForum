package MyForum.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date: 2022/9/4
 * Time: 18:48
 *
 * @Author SillyBaka
 * Description：日志记录的切面
 **/
@Aspect
@Component
@Slf4j
public class ServiceLogAscept {
    /**
     * 定义切入点  该方法将会切入表达式内定义的任何方法
     */
    @Pointcut("execution(* MyForum.service.*.*.*(..))")
    public void pointcut(){

    }

    /**
     * 方法执行之前
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        // 通过请求对象获得调用者的ip地址
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = request.getRemoteHost();
        // 日志内容： ip + 时间 + 调用接口
        //         用户[102.33.222]在[2022年9月4日19:05:57]调用了 [myforum.service.xxxx] 接口

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));

        String message = joinPoint.getSignature().getDeclaringTypeName()+ "." +
                joinPoint.getSignature().getName();

        log.info(String.format("用户 [%s] 在 [%s] 调用了 [%s] 接口",ip,time,message));
    }
}
