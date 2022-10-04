package MyForum.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Date: 2022/9/26
 * Time: 20:35
 *
 * @Author SillyBaka
 * Description：关注记录
 **/
@Data
@NoArgsConstructor
public class FollowRecord {

    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 关注的人id（关注别人）
     */
    private Long followId;
}
