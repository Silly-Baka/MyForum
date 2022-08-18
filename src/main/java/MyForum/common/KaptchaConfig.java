package MyForum.common;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Date: 2022/8/11
 * Time: 10:53
 *
 * @Author SillyBaka
 *
 * Description：Kaptcha验证码的配置类
 **/
@Configuration
public class KaptchaConfig {

    private static Properties properties;
    static {
        properties = new Properties();
        try(InputStream inputStream = KaptchaConfig.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Bean
    public Producer kaptchaProducer(){
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);

        kaptcha.setConfig(config);

        return kaptcha;
    }
}
