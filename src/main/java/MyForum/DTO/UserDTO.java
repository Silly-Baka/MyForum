package MyForum.DTO;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Date: 2022/8/9
 * Time: 21:01
 *
 * @Author SillyBaka
 * Description：
 **/
@Data
public class UserDTO{
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 头像链接
     */
    private String headerUrl;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 被点赞总数 （在redis中查）
     */
    private Integer likedCount;

    /**
     * 关注数
     */
    private Integer followCount;
    /**
     * 被关注数
     */
    private Integer followedCount;
    /**
     * 是否已被当前用户关注
     */
    private Boolean isFollowed;

    /**
     * 用户类型
     */
    private Integer type;
}
