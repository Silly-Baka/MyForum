package MyForum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

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
public class MyForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyForumApplication.class,args);
    }
}
