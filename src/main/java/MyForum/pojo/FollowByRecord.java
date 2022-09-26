package MyForum.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Date: 2022/9/26
 * Time: 20:37
 *
 * @Author SillyBaka
 * Description：被关注记录
 **/
@Data
@NoArgsConstructor
public class FollowByRecord {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 关注者id（关注该用户)
     */
    private Long followById;
}
