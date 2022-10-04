package MyForum.service;

import MyForum.DTO.Page;
import MyForum.pojo.Post;

/**
 * Date: 2022/10/2
 * Time: 15:51
 *
 * @Author SillyBaka
 * Description：
 **/
public interface SearchService {
    /**
     * 根据关键字和分页 查询分页的帖子信息
     * @param keyWord 关键字
     * @param currentPage 页号
     * @param pageSize 页大小
     * @return 根据关键字查询到的帖子的分页信息
     */
    Page<Post> searchPost(String keyWord, int currentPage, int pageSize);
}
