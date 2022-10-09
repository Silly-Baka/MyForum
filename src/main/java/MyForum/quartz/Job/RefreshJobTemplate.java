package MyForum.quartz.Job;

import MyForum.pojo.FollowByRecord;
import MyForum.pojo.FollowRecord;
import MyForum.pojo.LikeRecord;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Date: 2022/10/8
 * Time: 23:15
 *
 * @Author SillyBaka
 * Description：
 **/
public class RefreshJobTemplate {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    protected void getLikeRecordsFromRedis(List<LikeRecord> likeRecordList, List<Long> ids, int type, String redisKey , Set<String> deleteKeySet) {
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

    protected <T> void getFollowRecordFromRedis(Class<T> clazz, List<T> records, List<Long> userIds, Set<String> deleteKeySet, String keyPrefix){
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
