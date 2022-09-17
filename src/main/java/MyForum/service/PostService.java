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
}
