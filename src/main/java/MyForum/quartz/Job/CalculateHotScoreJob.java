package MyForum.quartz.Job;

import MyForum.pojo.Post;
import MyForum.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

import java.util.HashSet;
import java.util.Set;

import static MyForum.redis.RedisConstant.HOTTEST_POST_KEY;
import static MyForum.redis.RedisConstant.OPERATED_POST_KEY;

/**
 * Date: 2022/10/9
 * Time: 11:56
 *
 * @Author SillyBaka
 * Description：用于定时计算 热点帖子以及一段时间内被点赞、评论的帖子 的热度
 **/
@Slf4j
public class CalculateHotScoreJob implements Job {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private PostService postService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.info("正在执行定期任务 —— 更新某些帖子的热度");

        // 取出一段时间内被操作过的帖子ids
        Set<Object> operatedIds = redisTemplate.opsForSet().members(OPERATED_POST_KEY);

        // 取出热点帖子的ids
        Long size = redisTemplate.opsForZSet().size(HOTTEST_POST_KEY);
        Set<Object> hotIds = redisTemplate.opsForZSet().distinctRandomMembers(HOTTEST_POST_KEY, size);

        Set<Long> ids = new HashSet<>();

        if(operatedIds != null){
            operatedIds.forEach(id->{
                ids.add((Long) id);
            });
        }
        if(hotIds != null){
            hotIds.forEach(id->{
                ids.add((Long) id);
            });
        }

        // 为这些帖子重新计算热度值
        // 热度公式： hotScore = log10(是否加精 + 评论*10 + 点赞*2）+（发布时间-网站开启时间）
        for(Long id : ids){
            Post post = postService.getPostById(id);
            // 是否是精华帖 若true 则权重+75
            double isWonderful = post.getStatus() == 1 ? 75 : 0;
            // 评论数
            Integer commentCount = post.getCommentCount();
            // 点赞数
        }
    }
}
