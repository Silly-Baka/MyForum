package MyForum.service;

import MyForum.DTO.Page;
import MyForum.pojo.Post;

import java.util.Map;

/**
 * Date: 2022/8/8
 * Time: 12:43
 *
 * @Author SillyBaka
 * Description：
 **/
public interface PostService {
    Page<Post> getPostList(Integer currentPage);

    Page<Post> getHotPosts(Integer currentPage);

    void publishPost(String title,String content);

    Post getPostById(Long postId);

    Page<Post> getPostListByUserId(Long userId, Integer currentPage);

    /**
     * @return true-成功点赞  false-取消点赞
     */
    Map<String,Object> likePost(Long postId);

    /**
     * 加精目标帖子
     * @param postId 帖子id
     * @param status 帖子当前状态 0-未加精 1-已加精
     */
    void setPostWonderful(Long postId, Integer status);

    /**
     * 置顶目标帖子
     * @param postId 帖子id
     * @param type 帖子当前类型 0-普通帖 1-置顶帖
     */
    void setPostTop(Long postId, Integer type);

    /**
     * 删除目标帖子
     * @param postId 目标帖子id
     */
    void deletePost(Long postId);
}
