package MyForum.service;

import MyForum.DTO.CommentDTO;
import MyForum.DTO.Page;
import MyForum.pojo.Comment;

import java.util.List;
import java.util.Map;

/**
 * Date: 2022/8/30
 * Time: 15:27
 *
 * @Author SillyBaka
 * Description：
 **/
public interface CommentService {
    /**
     * 根据帖子的id获取该帖子的所有评论
     * @param postId 帖子id
     * @param currentPage 当前页号
     * @return 所有评论列表
     */
    Page<CommentDTO> getCommentListByPostId(Long postId, Integer currentPage);

    /**
     * 添加帖子评论
     * @param postId 帖子id
     * @param comment 评论
     */
    void addPostComment(Long postId, Comment comment);

    /**
     * 获取用户的所有回帖
     * @param userId 用户id
     * @param currentPage 页号
     * @return 回帖列表
     */
    Page<CommentDTO> getCommentListByUserId(Long userId,Integer currentPage);
}
