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
     * 按照热度顺序获取100条帖子
     */
    List<Post> getHotPosts();

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

    /**
     * 获取帖子的点赞量
     * @param postId 帖子id
     * @return 帖子的点赞数
     */
    Integer getScoreByPostId(Long postId);

    /**
     * 按照属性动态更新post
     */
    Integer updatePostDynamic(Post post);

    /**
     * 增加或减少点赞量
     * @param postId 帖子id
     * @param incr 为true就自增1 false就自减1
     */
    Integer incrScoreByPostId(Long postId,boolean incr);

    /**
     * 获得所有帖子的id
     * @return 所有帖子的id列表
     */
    List<Long> getAllPostId();

    // 获得所有帖子 -- 用于同步数据 仅测试环境下用
    List<Post> getAllPost();

    /**
     * 添加帖子
     * @param post 帖子
     */
    void addPost(Post post);

    void deletePost(Long postId);
}
