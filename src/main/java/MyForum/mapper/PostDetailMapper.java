package MyForum.mapper;

import MyForum.pojo.PostDetail;
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
public interface PostDetailMapper {
    /**
     * 主页默认获取帖子的接口
     * @param currentPage 当前页号
     * @param pageSize 页大小
     */
    List<PostDetail> getPostList(@Param("currentPage") Integer currentPage,
                                 @Param("pageSize") Integer pageSize);
    /**
     * 获取数据总条数
     * @return 数据总条数
     */
    Integer getCount();

    /**
     * 按照热门顺序获取帖子
     * @param currentPage 当前页号
     * @param pageSize 页大小
     */
    List<PostDetail> getHotPosts(@Param("currentPage") Integer currentPage,
                                 @Param("pageSize") Integer pageSize);
}
