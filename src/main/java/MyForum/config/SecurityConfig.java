package MyForum.config;

import MyForum.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static MyForum.util.MyForumConstant.*;

/**
 * Date: 2022/10/4
 * Time: 18:33
 *
 * @Author SillyBaka
 * Description：Spring Security 配置类
 **/
@Configuration
@Slf4j
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // 给每种权限授权
        httpSecurity.authorizeRequests()
                // 未登录无法做以下请求
                .antMatchers(HttpMethod.GET,
                        "/message/**",
                        "/user/setting",
                        "/user",
                        "/common/like/**",
                        "/common/follow/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(HttpMethod.POST,
                        "/message/letter",
                        "/user/upload",
                        "/user/password/change",
                        "/post",
                        "/comment/{postId}")
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers("/post/top/**",
                        "/post/wonderful/**")
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers("/post/delete/**",
                        "/data/**")
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();

        //没有登录或者是权限不够时的处理流程
        httpSecurity.exceptionHandling()
                // 在验证入口点发生异常 --> 即没有登录时
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

                        log.info("尚未登录，请求:{} 已被拦截",request.getRequestURI());

                        String xRequestedWith = request.getHeader("x-requested-with");
                        // 异步请求 --> 返回错误的json串
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommonUtil.getJsonString(403,"您尚未登录"));
                        }else {
                        // 同步请求 --> 重定位到登陆页面
                            response.sendRedirect(request.getContextPath() + "/user/login");
                        }
                    }
                })
                // 权限不够时
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        log.info("当前用户权限不足，请求:{} 已被拦截",request.getRequestURI());

                        String xRequestedWith = request.getHeader("x-requested-with");
                        // 异步请求 --> 返回错误的json串
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommonUtil.getJsonString(403,"权限不足，访问失败"));
                        }else {
                            // 同步请求 --> 重定位到权限不足的页面
                            response.sendRedirect(request.getContextPath() + "/common/denied");
                        }
                    }
                });

        httpSecurity.logout().logoutUrl("/666666");

        return httpSecurity.build();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web)->web.ignoring().antMatchers("/resources/**",
                "/static/**");
    }
}
