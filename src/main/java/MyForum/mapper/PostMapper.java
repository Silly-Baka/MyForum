package MyForum.mapper;

import MyForum.pojo.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Date: 2022/8/8
 * Time: 12:19
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface PostMapper {
    /**
     * 主页默认获取帖子的接口
     * @param offset 偏移量
     * @param pageSize 页大小
     */
    List<Post> getPostList(@Param("offset") Integer offset,
                           @Param("pageSize") Integer pageSize);
    /**
     * 获取数据总条数
     * @param userId 用户id
     * @return 数据总条数
     */
    Integer getCount(Long userId);

    /**
     * 按照热门顺序获取帖子
     * @param offset 当前页号
     * @param pageSize 页大小
     */
    List<Post> getHotPosts(@Param("offset") Integer offset,
                           @Param("pageSize") Integer pageSize);

    /**
     * 添加帖子
     * @param post 帖子
     */
    void addPost(Post post);

    /**
     * 根据帖子id获取帖子
     * @param id 帖子id
     * @return 帖子
     */
    Post getPostById(Long id);

    /**
     * 根据用户id获得该用户发的所有帖子
     * @param userId 用户id
     * @return 帖子列表
     */
    List<Post> getPostListByUserId(Long userId, Integer offset, Integer pageSize);
}
