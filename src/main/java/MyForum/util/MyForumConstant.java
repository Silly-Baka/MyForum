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

    // 消息类型的常量
    /**
     * 私信
     */
    public static final int MESSAGE_TYPE_LETTER = 0;
    /**
     * 关注
     */
    public static final int MESSAGE_TYPE_FOLLOW = 1;

}
