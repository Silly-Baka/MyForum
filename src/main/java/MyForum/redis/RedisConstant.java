package MyForum.redis;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Date: 2022/9/3
 * Time: 21:50
 *
 * @Author SillyBaka
 * Description：
 **/
public class RedisConstant {
    /**
     * 验证码key前缀 + sessionId
     */
    public static final String LOGIN_CODE_KEY = "login:code:";
    /**
     * 验证码过期时间 默认为300秒
     */
    public static final long LOGIN_CODE_EXPIRED_TIME = 300L;
    /**
     * 用户登陆token前缀
     */
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    /**
     * 登陆token过期时间 默认为30分钟
     */
    public static final long LOGIN_TOKEN_EXPIRED_TIME = 30L;
    /**
     * 忘记密码验证码key前缀
     */
    public static final String FORGET_CODE_KEY = "forget:code:";
    /**
     * 忘记密码验证码过期时间 默认为300秒
     */
    public static final long FORGET_CODE_EXPIRED_TIME = 300L;
    /**
     * 单个帖子缓存的key + postId
     */
    public static final String CACHE_POST_KEY = "cache:post:";
    /**
     * 帖子缓存的过期时间  600s
     */
    public static final long CACHE_POST_EXPIRED_TIME = 600L;
    /**
     * 分页帖子缓存的key + {用户id} +页号
     */
    public static final String CACHE_POST_PAGE_KEY = "cache:post:page:";
    /**
     * 帖子评论的分页数据缓存的key  + 帖子id + 当前页号
     */
    public static final String CACHE_COMMENT_PAGE_KEY = "cache:comment:page:";
    /**
     * 过期时间
     */
    public static final long CACHE_COMMENT_EXPIRED_TIME = 600L;

    /**
     * 帖子点赞的key   liked:post: + postId
     */
    public static final String LIKE_POST_KEY = "liked:post:";
    /**
     * 评论点赞的key  liked:comment: + commentId
     */
    public static final String LIKE_COMMENT_KEY = "liked:comment:";
    /**
     * 查看用户总被点赞数的key   liked:user:total: + userId
     */
    public static final String LIKE_USER_TOTAL_KEY = "liked:user:total:";

    /**
     * 某用户被关注列表的key  followed:user: + userId
     */
    public static final String FOLLOWED_BY_LIST_KEY = "followed:by:user:";
    /**
     * 某用户关注列表的key   follow:user: + userId
     */
    public static final String FOLLOW_LIST_KEY = "follow:user:";

    /**
     * 统计每一天的日活跃用户的key  count:daily:activeUser: + 日期 yyyy:MM:ss
     */
    public static final String COUNT_DAILY_ACTIVE_USER = "count:daily:activeUser:";

    public static final String COUNT_UV_DAILY = "count:uv:daily:";

    public static final String COUNT_UV_SECTION = "count:uv:section:";

    /**
     * 记录在一定时间内被操作过的帖子id的key，是Set类型的
     */
    public static final String OPERATED_POST_KEY = "operated:post";

    /**
     * 记录前100条热点帖子，数据结构为Zset value存帖子id score存帖子热度
     */
    public static final String HOTTEST_POST_KEY = "hottest:post";

    //-------- 以下为存放RabbitMQ消息的key -------------

    // 用于存放 异步通知业务 原业务记录的key
    public static final String EVENT_MESSAGE_ORIGIN_KEY = "event:message:origin";
    // 用于存放 异步通知业务 检查服务的key   该记录中的值应与原记录中的值定期检查同步
    public static final String EVENT_MESSAGE_CHECK_KEY = "event:message:check";
    // 业务消息唯一id
    public static final String EVENT_MESSAGE_ONLY_ID_KEY = "event:message:onlyId";


    // 日期格式化串
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    public static String getCountUVDaily(LocalDate date){
        return COUNT_UV_DAILY + date.format(FORMATTER);
    }

    public static String getCountUVSection(LocalDate startDate, LocalDate endDate){
        return COUNT_UV_SECTION + startDate.format(FORMATTER) + "_" + endDate.format(FORMATTER);
    }


}
