package MyForum.quartz;

import MyForum.mapper.*;
import MyForum.pojo.FollowByRecord;
import MyForum.pojo.FollowRecord;
import MyForum.pojo.LikeRecord;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static MyForum.redis.RedisConstant.*;
import static MyForum.util.MyForumConstant.ENTITY_TYPE_COMMENT;
import static MyForum.util.MyForumConstant.ENTITY_TYPE_POST;

/**
 * Date: 2022/9/26
 * Time: 15:18
 *
 * @Author SillyBaka
 * Description： 任务管理器
 **/
@Component
@Slf4j
public class JobManager {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 定期从redis中获得点赞数据并持久化到mysql的任务
     */
    private class LikeJob extends QuartzJobBean{

        @Override
        @Transactional
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

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
//            redisTemplate.delete(deleteKeySet);

            log.info("定时刷新点赞任务 已完成，已结束....");
        }

        private void getLikeRecordsFromRedis(List<LikeRecord> likeRecordList, List<Long> ids, int type, String redisKey ,Set<String> deleteKeySet) {
            for (Long id : ids) {
                String key = redisKey + id;

                Set<Object> likeUserIds = redisTemplate.opsForSet().members(key);
                if (likeUserIds == null) {
                    continue;
                }
                deleteKeySet.add(key);

                for (Object likeUserId : likeUserIds) {
                    LikeRecord likeRecord = new LikeRecord();
                    likeRecord.setEntityId(id);
                    likeRecord.setUserId(Long.valueOf(String.valueOf(likeUserId)));
                    likeRecord.setType(type);

                    likeRecordList.add(likeRecord);
                }
            }
        }
    }

    /**
     * 定期刷新关注数据的定时任务
     */
    private class FollowJob extends QuartzJobBean{
        @SneakyThrows
        @Override
        @Transactional
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            log.info("定期刷新关注数据 -- 任务开始....");

            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

            List<Long> userIds = userMapper.selectAllUserIds();

            // 记录要插入数据库的关注记录 -- 等待批量插入
            List<FollowRecord> followRecordList = new ArrayList<>();

            // 存放key的set 用于删除数据
            Set<String> deleteKeySet = new HashSet<>();

            // 先处理关注列表
//            getFollowRecordFromRedis(userIds, followRecordList, deleteKeySet);

            // 同步进数据库
            FollowRecordMapper followRecordMapper = sqlSession.getMapper(FollowRecordMapper.class);

            for(FollowRecord followRecord : followRecordList){
                followRecordMapper.addFollowRecord(followRecord);
            }

            // 处理被关注列表
            List<FollowByRecord> followByRecordList = new ArrayList<>();

//            getFollowRecordFromRedis(userIds,followByRecordList,deleteKeySet);
            getFollowRecordFromRedis(FollowByRecord.class,followByRecordList,userIds,deleteKeySet,FOLLOWED_BY_LIST_KEY);

            // 同步进数据库
            FollowByRecordMapper followByRecordMapper = sqlSession.getMapper(FollowByRecordMapper.class);
            for(FollowByRecord followByRecord : followByRecordList){
                followByRecordMapper.addFollowByRecord(followByRecord);
            }

            sqlSession.commit();
            sqlSession.close();

            // 都同步完成 就删redis
//            redisTemplate.delete(deleteKeySet);

            log.info("定期刷新关注任务 已完成 -- 任务结束....");
        }

        private <T> void getFollowRecordFromRedis(Class<T> clazz, List<T> records, List<Long> userIds, Set<String> deleteKeySet, String keyPrefix){
            for(long userId : userIds){
                String key = keyPrefix + userId;
                Set<Object> entityIds = redisTemplate.opsForSet().members(key);

                if(entityIds == null){
                    continue;
                }
                deleteKeySet.add(key);
                for(Object entityId : entityIds){
                    // 关注记录
                    if(clazz == FollowRecord.class){
                        FollowRecord followRecord = new FollowRecord();
                        followRecord.setUserId(userId);
                        followRecord.setFollowId(Long.valueOf(String.valueOf(entityId)));

                        records.add((T)followRecord);
                    // 被关注记录
                    }else if(clazz == FollowByRecord.class){
                        FollowByRecord followByRecord = new FollowByRecord();
                        followByRecord.setUserId(userId);
                        followByRecord.setFollowById(Long.valueOf(String.valueOf(entityId)));

                        records.add((T)followByRecord);
                    }
                }
            }
        }
    }


    @Bean
    public JobDetail likeJobDetail(){
        return JobBuilder.newJob(LikeJob.class)
                .withIdentity("likeJob","myforum")
                .requestRecovery(true)
                .storeDurably(true)
                .build();
    }

    @Bean
    public Trigger likeJobTrigger(JobDetail likeJobDetail){
        return TriggerBuilder.newTrigger()
                .withIdentity("likeJobTrigger","myforum")
                .forJob(likeJobDetail)
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInHours(1)
                        .repeatForever())
                .build();
    }

    @Bean
    public JobDetail followJobDetail(){
        return JobBuilder.newJob(FollowJob.class)
                .withIdentity("followJob","myforum")
                .requestRecovery(true)
                .storeDurably(true)
                .build();
    }
    @Bean
    public Trigger followJobTrigger(JobDetail followJobDetail){
        return TriggerBuilder.newTrigger()
                .forJob(followJobDetail)
                .withIdentity("followJobTrigger","myforum")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(1)
                        .repeatForever())
                .build();
    }
}
