package MyForum.common;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Date: 2022/8/8
 * Time: 20:38
 *
 * @Author SillyBaka
 *
 * Description：配置类
 **/
@Slf4j
public class Config {
    private static Properties properties;
    static {
        try(InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            log.debug("配置文件加载失败！",e);
        }
    }
    public static Integer getPageSize(){
        String property = properties.getProperty("page.size");
        if(property == null){
            return 10;
        }else {
            return Integer.parseInt(property);
        }
    }
    public static Integer getMaxPerPage(){
        String property = properties.getProperty("page.maxPerPage");
        if(property == null){
            return 5;
        }else {
            return Integer.parseInt(property);
        }
    }
}
