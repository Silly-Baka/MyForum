package MyForum.mapper;

import MyForum.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * Date: 2022/8/29
 * Time: 23:40
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface CommentMapper {
    List<Comment> selectCommentListByTypeAndEntityId(@Param("type") Integer type, @Param("entityId") Long entityId,
                                                  @Param("offset") Integer offset,@Param("pageSize") Integer pageSize);

    Comment selectCommentById(Long id);

    Integer getCount(@Param("userId") Long userId,@Param("type") Integer type, @Param("entityId") Long entityId);

    void addComment(Comment comment);

    List<Comment> getCommentListByUserId(@Param("userId") Long userId,
                                         @Param("offset") Integer offset,
                                         @Param("pageSize") Integer pageSize);

    /**
     * 获取所有评论的id
     */
    List<Long> getAllCommentIds();
}
