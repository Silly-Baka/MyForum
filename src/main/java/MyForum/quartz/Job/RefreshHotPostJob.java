package MyForum.quartz.Job;

import MyForum.mapper.PostMapper;
import MyForum.pojo.Post;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;
import java.util.*;

import static MyForum.redis.RedisConstant.HOTTEST_POST_KEY;

/**
 * Date: 2022/10/9
 * Time: 15:27
 *
 * @Author SillyBaka
 * Description：用于刷新热帖的定时任务
 **/
@Slf4j
public class RefreshHotPostJob implements Job {
    @Resource
    private PostMapper postMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("正在执行刷新热帖列表的定时任务.....");
        // 先从db中获取100条热度较高的帖子
        List<Post> hotPosts = postMapper.getHotPosts();
        Long size = redisTemplate.opsForZSet().size(HOTTEST_POST_KEY);

        // 旧的热帖列表
        Set<ZSetOperations.TypedTuple<Object>> oldHotPostSet ;

        if(size == null || size == 0){
            log.info("服务器中尚未有热点帖子，待添加");
            oldHotPostSet = new HashSet<>();
        }else {
            oldHotPostSet = redisTemplate.opsForZSet().reverseRangeWithScores(HOTTEST_POST_KEY,0,size-1);
        }

        if(oldHotPostSet == null){
            log.error("服务端出现异常，无法查询到热帖列表。定时任务失败");
            return;
        }

        List<ZSetOperations.TypedTuple<Object>> oldHotPostList = new ArrayList<>(oldHotPostSet);
        Collections.sort(oldHotPostList,(o1, o2) -> {
            return Double.compare(o1.getScore(),o2.getScore());
        });

        for(Post post : hotPosts){
            Long postId = post.getId();
            boolean isContain = false;
            int index;

            for (index = 0; index < oldHotPostList.size(); index++) {
                if(oldHotPostList.get(index).getValue() == postId){
                    isContain = true;
                    break;
                }
            }
            // 如果在redis中存在 则更新它的热度
            if(isContain){

                redisTemplate.opsForZSet().remove(HOTTEST_POST_KEY,postId);
                redisTemplate.opsForZSet().add(HOTTEST_POST_KEY,postId,post.getScore());

                ZSetOperations.TypedTuple<Object> newPost = new DefaultTypedTuple<>(postId, post.getScore());

                oldHotPostList.set(index,newPost);

            }else {
            // 如果在redis中不存在 则查看size是否为100
                size = redisTemplate.opsForZSet().size(HOTTEST_POST_KEY);
                size = size != null ? size : 0;

                // 如果size为100 则替换掉热度较低的帖子
                if(size == 100){
                    ZSetOperations.TypedTuple<Object> oldPost = oldHotPostList.get(0);

                    // 热度要比当前热度最低的帖子高才能替换
                    if(post.getScore() >= oldPost.getScore()){
                        // 删掉热度低的
                        redisTemplate.opsForZSet().remove(HOTTEST_POST_KEY,oldPost.getValue());
                        // 插入新的
                        redisTemplate.opsForZSet().add(HOTTEST_POST_KEY,postId,post.getScore());

                        ZSetOperations.TypedTuple<Object> newPost = new DefaultTypedTuple<>(postId, post.getScore());
                        oldHotPostList.set(0,newPost);
                    }

                }else {
                // 否则直接放入redis
                    redisTemplate.opsForZSet().add(HOTTEST_POST_KEY,postId,post.getScore());

                    ZSetOperations.TypedTuple<Object> newPost = new DefaultTypedTuple<>(postId, post.getScore());
                    oldHotPostList.add(newPost);
                }

                Collections.sort(oldHotPostList,(o1, o2) -> {
                    return Double.compare(o1.getScore(),o2.getScore());
                });
            }

        }
    }
}
