package MyForum.common;

import lombok.SneakyThrows;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Date: 2022/9/26
 * Time: 14:55
 *
 * @Author SillyBaka
 * Description：
 **/
//@Configuration
public class QuartzConfig implements SchedulerFactoryBeanCustomizer {

    @SneakyThrows
    @Override
    public void customize(SchedulerFactoryBean schedulerFactoryBean) {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();

        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();

        schedulerFactoryBean.setQuartzProperties(propertiesFactoryBean.getObject());
        schedulerFactoryBean.setStartupDelay(2);
    }
}
