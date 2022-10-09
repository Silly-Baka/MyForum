package MyForum.quartz.Job;

import MyForum.mapper.*;
import MyForum.pojo.FollowByRecord;
import MyForum.pojo.FollowRecord;
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

import static MyForum.redis.RedisConstant.FOLLOWED_BY_LIST_KEY;
import static MyForum.redis.RedisConstant.FOLLOW_LIST_KEY;

/**
 * Date: 2022/10/8
 * Time: 23:18
 *
 * @Author SillyBaka
 * Description：定期刷新关注数据的定时任务
 **/
@Slf4j
public class FollowRefreshJob extends RefreshJobTemplate implements Job {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserMapper userMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("定期刷新关注数据 -- 任务开始....");

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        List<Long> userIds = userMapper.selectAllUserIds();

        // 记录要插入数据库的关注记录 -- 等待批量插入
        List<FollowRecord> followRecordList = new ArrayList<>();

        // 存放key的set 用于删除数据
        Set<String> deleteKeySet = new HashSet<>();

        // 先处理关注列表
        getFollowRecordFromRedis(FollowRecord.class,followRecordList, userIds, deleteKeySet, FOLLOW_LIST_KEY);

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
        redisTemplate.delete(deleteKeySet);

        log.info("定期刷新关注任务 已完成 -- 任务结束....");
    }
}
