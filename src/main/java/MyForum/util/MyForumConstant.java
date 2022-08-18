package MyForum.util;

import java.time.Duration;

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

    //以下是Redis的常量
    /**
     * 验证码key前缀 + sessionId
     */
    public static final String LOGIN_CODE_KEY = "login:code:";
    /**
     * 验证码过期时间 默认为300秒
     */
    public static final Duration LOGIN_CODE_EXPIRED_TIME = Duration.ofSeconds(300);
    /**
     * 用户登陆token前缀
     */
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    /**
     * 登陆token过期时间 默认为30分钟
     */
    public static final Duration LOGIN_TOKEN_EXPIRED_TIME = Duration.ofMinutes(30);

}
