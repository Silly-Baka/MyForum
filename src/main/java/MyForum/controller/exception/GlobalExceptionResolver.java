package MyForum.controller.exception;

import MyForum.util.CommonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Date: 2022/9/4
 * Time: 17:04
 *
 * @Author SillyBaka
 * Description：
 **/
@Configuration
@Slf4j
public class GlobalExceptionResolver implements HandlerExceptionResolver {
    @SneakyThrows
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 记录异常信息的日志
        log.error("服务器发生异常:{}",ex.getMessage());
        ex.printStackTrace();
        // 设置异常的视图处理
        ModelAndView mv = new ModelAndView();

        mv.setViewName("site/error/500");
        String xRequesterWith = request.getHeader("x-requested-with");

        if("XMLHttpRequest".equals(xRequesterWith)){
        // 如果是异步请求  响应异常信息的json字符串给前端
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommonUtil.getJsonString(1, ex.getMessage()));
        }else {
        // 否则就是普通请求 将异常信息放入视图中
            mv.addObject("errorMsg",ex.getMessage());
        }
        return mv;
    }
}
