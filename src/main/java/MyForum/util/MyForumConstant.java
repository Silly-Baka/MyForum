package MyForum.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static MyForum.redis.RedisConstant.LIKE_COMMENT_KEY;
import static MyForum.redis.RedisConstant.LIKE_POST_KEY;

/**
 * Date: 2022/8/10
 * Time: 15:07
 *
 * @Author SillyBaka
 *
 * Description：用于存放各种常量
 **/
public class MyForumConstant {
    /**
     * 成功激活
     */
    public static final int ACTIVATION_SUCCESS = 1;
    /**
     * 激活失败
     */
    public static final int ACTIVATION_FAIL = 0;
    /**
     * 成功登出
     */
    public static final int USER_LOGOUT_SUCCESS = 1;
    /**
     * 登出失败
     */
    public static final int USER_LOGOUT_FAIL = 0;
    /**
     * 帖子的一级评论
     */
    public static final int COMMENT_TYPE_FIRST = 1;
    /**
     * 二级评论（评论的评论）
     */
    public static final int COMMENT_TYPE_SECOND = 2;

    // 消息（通知）类型的常量
    /**
     * 私信
     */
    public static final int MESSAGE_TYPE_LETTER = 0;
    /**
     * 关注
     */
    public static final int MESSAGE_TYPE_FOLLOW = 1;
    /**
     * 点赞
     */
    public static final int MESSAGE_TYPE_LIKE = 2;
    /**
     * 评论
     */
    public static final int MESSAGE_TYPE_COMMENT = 3;


    // 实体类型对应常量-entityType
    /**
     * 帖子类型对应的常量
     */
    public static final int ENTITY_TYPE_POST = 1;
    /**
     * 评论类型对应的常量
     */
    public static final int ENTITY_TYPE_COMMENT = 2;


    // entityType -- Redis中点赞的key 的映射map
    public static final Map<Integer,String> ENTITY_TYPE_REDIS_LIKE_KEY_MAP = new HashMap<>();

    // 事件类型常量
    /**
     * 评论事件
     */
    public static final int EVENT_TYPE_COMMENT = 123;
    /**
     * 点赞事件
     */
    public static final int EVENT_TYPE_LIKE = 666;
    /**
     * 关注事件
     */
    public static final int EVENT_TYPE_FOLLOW = 777;

    public static final Map<Integer,Integer> EVENT_TYPE_TO_MESSAGE_TYPE_MAP = new HashMap<>();

    public static final Set<Integer> NOTICE_TYPE_SET = new HashSet<>();

    public static final Map<Integer,String> MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME = new HashMap<>();

    static {
        // 初始化 Redis中实体类型和点赞key的映射
        ENTITY_TYPE_REDIS_LIKE_KEY_MAP.put(ENTITY_TYPE_POST,LIKE_POST_KEY);
        ENTITY_TYPE_REDIS_LIKE_KEY_MAP.put(ENTITY_TYPE_COMMENT,LIKE_COMMENT_KEY);

        // 初始化 事件类型和消息类型的映射
        EVENT_TYPE_TO_MESSAGE_TYPE_MAP.put(EVENT_TYPE_COMMENT,MESSAGE_TYPE_COMMENT);
        EVENT_TYPE_TO_MESSAGE_TYPE_MAP.put(EVENT_TYPE_FOLLOW,MESSAGE_TYPE_FOLLOW);
        EVENT_TYPE_TO_MESSAGE_TYPE_MAP.put(EVENT_TYPE_LIKE,MESSAGE_TYPE_LIKE);

        // 初始化 通知类型的总列表
        NOTICE_TYPE_SET.add(MESSAGE_TYPE_FOLLOW);
        NOTICE_TYPE_SET.add(MESSAGE_TYPE_LIKE);
        NOTICE_TYPE_SET.add(MESSAGE_TYPE_COMMENT);

        // 初始化 消息类型和消息类型名的映射
        MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME.put(MESSAGE_TYPE_LETTER,"letter");
        MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME.put(MESSAGE_TYPE_FOLLOW,"follow");
        MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME.put(MESSAGE_TYPE_LIKE,"like");
        MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME.put(MESSAGE_TYPE_COMMENT,"comment");
    }

    // 权限管理

    // 普通用户
    public static final String AUTHORITY_USER = "user";
    // 管理员
    public static final String AUTHORITY_ADMIN = "admin";
    // 版主
    public static final String AUTHORITY_MODERATOR = "moderator";
}
