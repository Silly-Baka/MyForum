package MyForum.pojo;

import MyForum.DTO.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Date: 2022/8/8
 * Time: 12:04
 *
 * @Author SillyBaka
 *
 * Description：帖子详细信息
 **/
@Data
@NoArgsConstructor
public class Post implements Serializable {
    /**
     * 帖子id
     */
    private Long id;
    /**
     * 发帖子的用户id
     */
    private Long userId;
    /**
     * 帖子标题
     */
    private String title;
    /**
     * 帖子内容
     */
    private String content;
    /**
     * 帖子类型 0为普通贴 1为置顶贴
     */
    private Integer type;
    /**
     * 帖子状态 0-正常  1-精品  2-撤销、拉黑
     */
    private Integer status;
    /**
     * 帖子创建时间
     */
    private LocalDateTime createTime;
    /**
     * 点赞数
     */
    private Integer score;
    /**
     * 回帖数量
     */
    private Integer commentCount;
    /**
     * 用户信息
     */
    private UserDTO user;
    /**
     * 当前用户已经点赞
     */
    private Boolean isLiked;
}
