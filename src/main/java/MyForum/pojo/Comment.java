package MyForum.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Date: 2022/8/29
 * Time: 22:58
 *
 * @Author SillyBaka
 * Description：帖子评论类
 **/
@Data
@NoArgsConstructor
public class Comment implements Serializable {
    /**
     * 唯一id
     */
    private Long id;
    /**
     * 发出该评论的用户id
     */
    private Long userId;
    /**
     * 评论类型 1 -- 一级评论（评论帖子）  2-- 二级评论（评论的评论）
     */
    private Integer type;
    /**
     * 所在类型的帖子id
     */
    private Long entityId;
    /**
     * 回复谁的评论 的userId（没有则为0)
     */
    private Long targetId;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论状态
     */
    private Integer status;
    /**
     * 评论创建时间
     */
    private LocalDateTime createTime;
    /**
     * 点赞数
     */
    private Integer likeCount;
}
