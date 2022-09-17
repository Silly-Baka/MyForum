package MyForum.DTO;

import MyForum.pojo.Comment;
import MyForum.pojo.Post;
import lombok.Data;

import java.util.List;

/**
 * Date: 2022/9/6
 * Time: 16:40
 *
 * @Author SillyBaka
 * Description：
 **/
@Data
public class CommentDTO extends Comment {

    /**
     * 发起者的用户信息
     */
    private UserDTO user;
    /**
     * 二级评论列表 （只有一级评论才有）
     */
    private List<CommentDTO> commentList;
    /**
     * 回复目标用户的信息
     */
    private UserDTO targetUser;
    /**
     * 是否已被当前用户点赞
     */
    private boolean isLiked;
    /**
     * 评论所在帖子的信息
     */
    private Post post;
}
