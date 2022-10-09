package MyForum.config;

import MyForum.interceptor.AccessRecordInterceptor;
import MyForum.interceptor.LoginInterceptor;
import MyForum.interceptor.RefreshTokenInterceptor;
import MyForum.interceptor.UnreadCountInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

/**
 * Date: 2022/8/17
 * Time: 17:47
 *
 * @Author SillyBaka
 * Description：MVC的配置类
 **/
@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {

    @Resource
    private LoginInterceptor loginInterceptor;
    @Resource
    private RefreshTokenInterceptor refreshTokenInterceptor;
    @Resource
    private UnreadCountInterceptor unreadCountInterceptor;
    @Resource
    private AccessRecordInterceptor accessRecordInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(refreshTokenInterceptor)
                .addPathPatterns("/**")
                .order(-1);

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/index",
                        "/user/login",
                        "/user/register",
                        "/user/forget",
                        "/common/**",
                        "/activation/**",
                        "/post/**",
                        "/static/**",
                        "/user/header/**",
                        "/user/profile/**"
                );

        registry.addInterceptor(unreadCountInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(accessRecordInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 防止拦截器拦截掉静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
