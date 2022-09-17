package MyForum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Date: 2022/8/8
 * Time: 11:58
 *
 * @Author SillyBaka
 * Description：
 **/
@SpringBootApplication
@MapperScan(basePackages = {"MyForum.mapper"})
@EnableCaching
@EnableAspectJAutoProxy
public class MyForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyForumApplication.class,args);
    }
}
