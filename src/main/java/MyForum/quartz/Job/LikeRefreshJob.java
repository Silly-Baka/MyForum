package MyForum.quartz.Job;

import MyForum.mapper.CommentMapper;
import MyForum.mapper.LikeRecordMapper;
import MyForum.mapper.PostMapper;
import MyForum.pojo.LikeRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static MyForum.redis.RedisConstant.LIKE_COMMENT_KEY;
import static MyForum.redis.RedisConstant.LIKE_POST_KEY;
import static MyForum.util.MyForumConstant.ENTITY_TYPE_COMMENT;
import static MyForum.util.MyForumConstant.ENTITY_TYPE_POST;

/**
 * Date: 2022/10/8
 * Time: 23:14
 *
 * @Author SillyBaka
 * Description：定期从redis中获得点赞数据并持久化到mysql的任务
 **/
@Slf4j
public class LikeRefreshJob extends RefreshJobTemplate implements Job{

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private PostMapper postMapper;
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("定期刷新点赞任务 -- 已开始....");

        // 存放key的set 用于删除数据
        Set<String> deleteKeySet = new HashSet<>();

        // 开启批量处理模式
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        LikeRecordMapper mapper = sqlSession.getMapper(LikeRecordMapper.class);

        // 先处理帖子
        List<Long> postIds = postMapper.getAllPostId();
        // 暂存点赞记录 --> 批量插入数据库
        List<LikeRecord> likeRecordList = new ArrayList<>();
        getLikeRecordsFromRedis(likeRecordList, postIds, ENTITY_TYPE_POST, LIKE_POST_KEY, deleteKeySet);

        // 再处理评论
        List<Long> commentIds = commentMapper.getAllCommentIds();
        getLikeRecordsFromRedis(likeRecordList, commentIds, ENTITY_TYPE_COMMENT, LIKE_COMMENT_KEY, deleteKeySet);

        // 批量插入到数据库中
        likeRecordList.forEach(likeRecord -> {
            mapper.addLikeRecord(likeRecord);
        });

        sqlSession.commit();
        sqlSession.close();

        // 同步完后 删掉redis中的数据
        redisTemplate.delete(deleteKeySet);

        log.info("定时刷新点赞任务 已完成，已结束....");
    }
}
