package MyForum.quartz.Job;

import MyForum.mapper.PostMapper;
import MyForum.pojo.Post;
import MyForum.redis.RedisConstant;
import MyForum.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static MyForum.redis.RedisConstant.HOTTEST_POST_KEY;
import static MyForum.redis.RedisConstant.OPERATED_POST_KEY;
import static MyForum.util.MyForumConstant.START_DATE;

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
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.info("正在执行定期任务 —— 更新某些帖子的热度");

        // 取出一段时间内被操作过的帖子ids
        Set<Object> operatedIds = redisTemplate.opsForSet().members(OPERATED_POST_KEY);

        // 取出热点帖子的个数
        Long size = redisTemplate.opsForZSet().size(HOTTEST_POST_KEY);

        // 热点帖子的id列表
        Set<Object> hotIds = null;

        if(size == null || size == 0){
            log.info("服务器中尚未有热点帖子，待添加");
            hotIds = new HashSet<>();
        }else {
            hotIds = redisTemplate.opsForZSet().range(HOTTEST_POST_KEY,0,size-1);
        }

        Set<Long> ids = new HashSet<>();

        if(operatedIds != null){
            operatedIds.forEach(id->{
                ids.add(Long.valueOf(String.valueOf(id)));
            });
        }
        if(hotIds != null){
            hotIds.forEach(id->{
                ids.add(Long.valueOf(String.valueOf(id)));
            });
        }

        // 为这些帖子重新计算热度值
        // 热度公式： hotScore = log10(是否加精 + 评论*10 + 点赞*2）+（发布时间-网站开启时间）
        List<Post> updatePosts = new ArrayList<>();
        for(Long id : ids){
            Post post = postService.getPostById(id);
            // 是否是精华帖 若true 则权重+75
            double isWonderful = post.getStatus() == 1 ? 75 : 1;
            // 评论数
            Integer commentCount = post.getCommentCount();
            // 点赞数
            Integer likeCount = (Integer) redisTemplate.opsForValue().get(RedisConstant.getLikePostCountKey(id));
            likeCount = likeCount != null ? likeCount : 0;
            // 发布时间-网站开启时间
            long epochDif = post.getCreateTime().toEpochSecond(ZoneOffset.UTC)
                    - START_DATE.toEpochSecond(ZoneOffset.UTC);

            // 热度值
            double hotScore = Math.log10(isWonderful + commentCount*10 + likeCount*2) + epochDif / (1000*3600*24);

            post.setScore(hotScore);

            updatePosts.add(post);
        }
        // 批量更新
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        PostMapper postMapper = sqlSession.getMapper(PostMapper.class);

        for(Post post : updatePosts){
            postMapper.updatePostDynamic(post);
        }

        sqlSession.commit();
        sqlSession.close();

        // 删掉redis中的记录
        redisTemplate.delete(OPERATED_POST_KEY);
    }
}
