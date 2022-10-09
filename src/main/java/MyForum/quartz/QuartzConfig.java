package MyForum.quartz;

import MyForum.quartz.Job.CalculateHotScoreJob;
import MyForum.quartz.Job.FollowRefreshJob;
import MyForum.quartz.Job.LikeRefreshJob;
import MyForum.quartz.Job.RefreshHotPostJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * Date: 2022/9/26
 * Time: 14:55
 *
 * @Author SillyBaka
 * Description：
 **/
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean likeJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(LikeRefreshJob.class);
        factoryBean.setName("likeJob");
        factoryBean.setGroup("myforum");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }
    @Bean
    public SimpleTriggerFactoryBean likeJobTrigger(JobDetail likeJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(likeJobDetail);
        factoryBean.setName("likeJobTrigger");
        factoryBean.setGroup("myforum");
        // 两个小时
        factoryBean.setRepeatInterval(1000*60*60*2);
        factoryBean.setJobDataMap(new JobDataMap());

        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean followJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(FollowRefreshJob.class);
        factoryBean.setName("followJob");
        factoryBean.setGroup("myforum");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }
    @Bean
    public SimpleTriggerFactoryBean followJobTrigger(JobDetail followJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(followJobDetail);
        factoryBean.setName("followJobTrigger");
        factoryBean.setGroup("myforum");
        // 两个小时
        factoryBean.setRepeatInterval(1000*60*60*2);
        factoryBean.setJobDataMap(new JobDataMap());

        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean calculateHotScoreJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(CalculateHotScoreJob.class);
        factoryBean.setName("calculateHotScoreJobDetail");
        factoryBean.setGroup("myforum");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }
    @Bean
    public SimpleTriggerFactoryBean calculateHotScoreJobTrigger(JobDetail calculateHotScoreJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(calculateHotScoreJobDetail);
        factoryBean.setName("calculateHotScoreJobTrigger");
        factoryBean.setGroup("myforum");
        // 一小时
        factoryBean.setRepeatInterval(1000*60*60);
//        factoryBean.setRepeatInterval(1000*60);
        factoryBean.setJobDataMap(new JobDataMap());

        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean refreshHotPostJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(RefreshHotPostJob.class);
        factoryBean.setName("refreshHotPostJobDetail");
        factoryBean.setGroup("myforum");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }
    @Bean
    public SimpleTriggerFactoryBean refreshHotPostJobTrigger(JobDetail refreshHotPostJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(refreshHotPostJobDetail);
        factoryBean.setName("refreshHotPostJobTrigger");
        factoryBean.setGroup("myforum");
        // 一小时
        factoryBean.setRepeatInterval(1000*60*60);
//        factoryBean.setRepeatInterval(1000*60);
        factoryBean.setJobDataMap(new JobDataMap());

        return factoryBean;
    }
}
