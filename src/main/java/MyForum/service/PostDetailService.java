package MyForum.service;

import MyForum.DTO.Page;
import MyForum.pojo.PostDetail;

/**
 * Date: 2022/8/8
 * Time: 12:43
 *
 * @Author SillyBaka
 * Description：
 **/
public interface PostDetailService {
    Page<PostDetail> getPostList(Integer currentPage);

    Page<PostDetail> getHotPosts(Integer currentPage);
}
