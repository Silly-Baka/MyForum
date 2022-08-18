package com.example.myforum;

import MyForum.MyForumApplication;
import MyForum.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Date: 2022/8/9
 * Time: 18:50
 *
 * @Author SillyBaka
 * Description：
 **/
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(classes = MyForumApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void contextLoads(){
        mailClient.sendMail("1017165985@qq.com","TEST","我是你爹");
    }

    @Test
    void testHTMLMail(){
        Context context = new Context();
        context.setVariable("username","啊飞");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("928840730@qq.com","HTML",content);
    }
}
