package MyForum.service.impl;

import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.common.Config;
import MyForum.common.UserHolder;
import MyForum.mapper.CommentMapper;
import MyForum.mapper.UserMapper;
import MyForum.pojo.Comment;
import MyForum.pojo.User;
import MyForum.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static MyForum.util.MyForumConstant.*;
import static MyForum.util.RedisConstant.*;

/**
 * Date: 2022/8/30
 * Time: 15:27
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public Page<Comment> getCommentListByPostId(Long postId, Integer currentPage) {
        if(postId == null){
            throw new RuntimeException("传入的帖子id为空！获取评论列表失败");
        }
        // 先检查redis中有无缓存分页数据
        String key = CACHE_COMMENT_PAGE_KEY + postId + ":" + currentPage;
        Page<Comment> page = (Page<Comment>) redisTemplate.opsForValue().get(key);
        if(page != null){
            return page;
        }
        page = new Page<>(currentPage, commentMapper.getCount(null,COMMENT_TYPE_FIRST, postId));
        // 获取一级评论列表
        List<Comment> commentList = commentMapper.getCommentListByTypeAndEntityId(COMMENT_TYPE_FIRST, postId, (currentPage-1)*Config.getPageSize(), Config.getPageSize());
        for (Comment comment : commentList) {

            comment.setUsername(userMapper.selectUserById(comment.getUserId()).getUsername());
            Long entityId = comment.getId();
            // 获取每个一级评论的二级评论列表
            List<Comment> subCommentList = commentMapper.getCommentListByTypeAndEntityId(COMMENT_TYPE_SECOND, entityId , null , null);
            if(subCommentList != null){
                // 检查每个二级评论是否是回复 若是则加上回复目标用户的名字
                for (Comment subComment : subCommentList) {
                    subComment.setUsername(userMapper.selectUserById(subComment.getUserId()).getUsername());
                    if(subComment.getTargetId() != 0){
                        User user = userMapper.selectUserById(subComment.getTargetId());
                        subComment.setTargetName(user.getUsername());
                    }
                }
                comment.setCommentList(subCommentList);
            }
        }
        page.setRecords(commentList);

        redisTemplate.opsForValue().set(key,page,CACHE_COMMENT_EXPIRED_TIME, TimeUnit.SECONDS);

        return page;
    }

    @Override
    @Transactional
    public void addPostComment(Long postId,Comment comment) {
        if(comment == null){
            throw new RuntimeException("参数为空！添加失败");
        }
        UserDTO currentUser = UserHolder.getCurrentUser();
        comment.setUserId(currentUser.getId());
        comment.setType(COMMENT_TYPE_FIRST);
        comment.setEntityId(postId);
        comment.setCreateTime(LocalDateTime.now());
        // 将评论添加进数据库
        commentMapper.addComment(comment);
        // 计算得到最后一页的页号
        Integer totalCount = commentMapper.getCount(null,COMMENT_TYPE_FIRST, postId);
        Page<Object> page = new Page<>(totalCount);
        Integer lastPageNum = page.getTotal();
        // 再删除最后一页的缓存
        redisTemplate.delete(CACHE_COMMENT_PAGE_KEY + postId + ":" + lastPageNum);
    }

    @Override
    public Page<Comment> getCommentListByUserId(Long userId, Integer currentPage) {

        Integer total = commentMapper.getCount(userId, 1, null);
        Page<Comment> page = new Page<>(currentPage, total);
        List<Comment> commentList = commentMapper.getCommentListByUserId(userId, (currentPage - 1)*page.getPageSize(), page.getPageSize());

        page.setRecords(commentList);

        return page;
    }


}
