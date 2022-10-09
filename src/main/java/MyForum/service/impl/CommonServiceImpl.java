package MyForum.service.impl;

import MyForum.common.UserHolder;
import MyForum.pojo.EventMessage;
import MyForum.rabbitMQ.EventMessageProducer;
import MyForum.service.CommonService;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static MyForum.redis.RedisConstant.*;
import static MyForum.util.MyForumConstant.*;

/**
 * Date: 2022/9/5
 * Time: 21:58
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private EventMessageProducer eventMessageProducer;

    private static final DefaultRedisScript<Boolean> FOLLOW_SCRIPT;
    static {
        FOLLOW_SCRIPT = new DefaultRedisScript<>();
        FOLLOW_SCRIPT.setLocation(new ClassPathResource("lua/follow.lua"));
        FOLLOW_SCRIPT.setResultType(Boolean.class);
    }

    // lua脚本的script对象
    private static final DefaultRedisScript<Boolean> LIKE_SCRIPT;
    static {
        LIKE_SCRIPT = new DefaultRedisScript<>();
        // 设置lua脚本的路径
        LIKE_SCRIPT.setLocation(new ClassPathResource("lua/like.lua"));
        // 设置返回值类型
        LIKE_SCRIPT.setResultType(Boolean.class);
    }

    @Override
    public Map<String, Object> like(Integer entityType, Long entityId, Long entityUserId) {
        //todo 有待使用aop优化 缩减业务代码边幅
        // 发送消息给消息队列
        //获取当前用户信息
        Long currentUserId = UserHolder.getCurrentUser().getId();
        Map<String,Object> prop = new HashMap<>();
        prop.put("entityId",entityId);
        EventMessage eventMessage = eventMessageProducer.createEventMessage(EVENT_TYPE_LIKE, currentUserId, entityUserId, prop);
        eventMessageProducer.sendMessage(eventMessage);

        //todo 点赞帖子 记录该帖子
        if(entityType == ENTITY_TYPE_POST){
            redisTemplate.opsForSet().add(OPERATED_POST_KEY, entityId);
        }

        // -----------
        String keyPrefix = ENTITY_TYPE_REDIS_LIKE_KEY_MAP.getOrDefault(entityType,null);
        if(StrUtil.isBlank(keyPrefix)){
            throw new RuntimeException("服务端不存在该实体或实体类型参数异常！entityType = " + entityType);
        }
        Map<String,Object> result = new HashMap<>();
        // 根据实体类型获取到redis的key
        String likeKey = ENTITY_TYPE_REDIS_LIKE_KEY_MAP.get(entityType) + entityId;
        // 被点赞者的key
        String likeTotalKey = LIKE_USER_TOTAL_KEY + entityUserId;
        // 查询redis 看是否已点赞
        Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, currentUserId);
        //todo 已优化成lua脚本
        // 执行lua脚本
        Boolean likeStatus = redisTemplate.execute(
                LIKE_SCRIPT,
                Arrays.asList(likeKey,likeTotalKey),
                currentUserId
        );
        result.put("likeStatus",likeStatus);
//        // 尚未点赞 则点赞
//        if(BooleanUtil.isFalse(isLiked)){
//            //todo 将两条命令优化成lua脚本
//            redisTemplate.opsForSet().add(likeKey,currentUserId);
//            // 增加发起者的被点赞总数
//            redisTemplate.opsForValue().increment(likeTotalKey);
//            result.put("likeStatus",1);
//        }else {
//        // 否则取消点赞
//            //todo 将两条命令优化成lua脚本
//            redisTemplate.opsForSet().remove(likeKey,currentUserId);
//            // 减少发起者的点赞总数
//            redisTemplate.opsForValue().decrement(likeTotalKey);
//            result.put("likeStatus",0);
//        }
        // 获取当前实体的点赞总数
        Long likeCount = redisTemplate.opsForSet().size(likeKey);
        result.put("likeCount",likeCount);

        return result;
    }

        // 已关注 就取消关注 减少本用户的关注列表 和被关注用户的被关注列表 返回 -1
        // 否则就关注 增加本用户的关注列表 和被关注用户的关注列表 并且返回 1
    @Override
    public Map<String, Object> follow(Long userId) {
        //todo 有待优化成aop
        // 发送消息给消息队列
        Long currentUserId = UserHolder.getCurrentUser().getId();
        EventMessage eventMessage = eventMessageProducer.createEventMessage(EVENT_TYPE_FOLLOW, currentUserId, userId, null);
        eventMessageProducer.sendMessage(eventMessage);


        Map<String,Object> result = new HashMap<>();
        // 被关注用户的被关注列表key
        String followedKey = FOLLOWED_BY_LIST_KEY + userId;
        // 当前用户的关注列表key
        String currentUserFollowKey = FOLLOW_LIST_KEY + currentUserId;
        //todo 这里用lua脚本 解决redis事务问题
        Boolean followStatus = redisTemplate.execute(
                FOLLOW_SCRIPT,
                Arrays.asList(currentUserFollowKey,followedKey),
                currentUserId , userId
        );
        result.put("followStatus",followStatus);

        // 查询目标用户被关注总数 并返回
        Long followedCount = redisTemplate.opsForSet().size(followedKey);

        result.put("followedCount",followedCount);

        return result;
    }

}
