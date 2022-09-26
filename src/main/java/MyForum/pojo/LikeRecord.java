package MyForum.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Date: 2022/9/26
 * Time: 16:23
 *
 * @Author SillyBaka
 * Description： 点赞实体类
 **/
@Data
@NoArgsConstructor
public class LikeRecord {
    private Long id;
    /**
     * 被点赞的实体id
     */
    private Long entityId;
    /**
     * 实体类型 0-帖子 1-评论
     */
    private Integer type;
    /**
     * 用户id
     */
    private Long userId;
}
