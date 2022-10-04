package MyForum.service.impl;

import MyForum.DTO.CommentDTO;
import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.config.Config;
import MyForum.common.UserHolder;
import MyForum.mapper.CommentMapper;
import MyForum.mapper.PostMapper;
import MyForum.mapper.UserMapper;
import MyForum.pojo.Comment;
import MyForum.pojo.EventMessage;
import MyForum.pojo.Post;
import MyForum.pojo.User;
import MyForum.rabbitMQ.EventMessageProducer;
import MyForum.service.CommentService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static MyForum.util.MyForumConstant.*;
import static MyForum.redis.RedisConstant.*;

/**
 * Date: 2022/8/30
 * Time: 15:27
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private EventMessageProducer eventMessageProducer;


    @Override
    public Page<CommentDTO> getCommentListByPostId(Long postId, Integer currentPage) {
        if(postId == null){
            throw new RuntimeException("传入的帖子id为空！获取评论列表失败");
        }
        // 先检查redis中有无缓存分页数据
        String key = CACHE_COMMENT_PAGE_KEY + postId + ":" + currentPage;
        Page<CommentDTO> page = (Page<CommentDTO>) redisTemplate.opsForValue().get(key);
        if(page != null){
            return page;
        }
        page = new Page<>(currentPage, commentMapper.getCount(null,COMMENT_TYPE_FIRST, postId));

        UserDTO currentUser = UserHolder.getCurrentUser();
        // 获取回帖列表
        List<Comment> commentList = commentMapper.selectCommentListByTypeAndEntityId(COMMENT_TYPE_FIRST, postId, (currentPage-1)*Config.getPageSize(), Config.getPageSize());
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment comment : commentList) {
            CommentDTO commentDTO = BeanUtil.copyProperties(comment, CommentDTO.class);
            // 给帖子设置用户信息
            commentDTO.setUser(BeanUtil.copyProperties(userMapper.selectUserById(comment.getUserId()),UserDTO.class));
            Long entityId = comment.getId();
            // 获取每个一级评论的二级评论列表
            List<Comment> subCommentList = commentMapper.selectCommentListByTypeAndEntityId(COMMENT_TYPE_SECOND, entityId , null , null);
            List<CommentDTO> subCommentDTOList = new ArrayList<>();
            if(subCommentList != null){
                // 检查每个二级评论是否是回复 若是则加上回复目标用户的名字
                for (Comment subComment : subCommentList) {
                    CommentDTO subCommentDTO = BeanUtil.copyProperties(subComment, CommentDTO.class);
                    // 给评论设置用户信息
                    subCommentDTO.setUser(BeanUtil.copyProperties(userMapper.selectUserById(subComment.getUserId()),UserDTO.class));

                    if(subCommentDTO.getTargetId() != 0){
                        User user = userMapper.selectUserById(subCommentDTO.getTargetId());
                        subCommentDTO.setTargetUser(BeanUtil.copyProperties(user,UserDTO.class));
                    }
                    getCommentLikeInfo(currentUser, subCommentDTO);
                    subCommentDTOList.add(subCommentDTO);
                }

                commentDTO.setCommentList(subCommentDTOList);
                getCommentLikeInfo(currentUser, commentDTO);
                commentDTOList.add(commentDTO);
            }
        }
        page.setRecords(commentDTOList);

        redisTemplate.opsForValue().set(key,page,CACHE_COMMENT_EXPIRED_TIME, TimeUnit.SECONDS);

        return page;
    }

    @Override
    public Page<CommentDTO> getCommentListByUserId(Long userId, Integer currentPage) {

        Integer total = commentMapper.getCount(userId, 1, null);
        Page<CommentDTO> page = new Page<>(currentPage, total);

        List<Comment> commentList = commentMapper.getCommentListByUserId(userId, (currentPage - 1)*page.getPageSize(), page.getPageSize());
        // 封装成dto
        List<CommentDTO> commentDTOList = new ArrayList<>();

        for (Comment comment : commentList) {
            // 用户个人主页只需要评论的基本信息 和 所在帖子的信息
            // 这里只需要封装所在帖子的信息
            Long postId = comment.getEntityId();
            Post post = postMapper.getPostById(postId);

            CommentDTO commentDTO = BeanUtil.copyProperties(comment, CommentDTO.class);
            commentDTO.setPost(post);

            commentDTOList.add(commentDTO);
        }

        page.setRecords(commentDTOList);

        return page;
    }

    /**
     * 从redis中获取评论的点赞信息 并设置到评论对象的属性中
     * @param currentUser 当前用户对象
     * @param commentDTO 评论对象
     */
    private void getCommentLikeInfo(UserDTO currentUser, CommentDTO commentDTO) {
        String likeKey = LIKE_COMMENT_KEY + commentDTO.getId();

        // 获取评论的点赞数
        Long likeCount = redisTemplate.opsForSet().size(likeKey);
        if (likeCount != null) {
            commentDTO.setLikeCount(likeCount.intValue());
        }
        // 查询当前用户是否已点赞

        Boolean isLiked = false;

        if (currentUser != null){
            isLiked = redisTemplate.opsForSet().isMember(likeKey, currentUser.getId());
        }
        if (isLiked != null) {
            commentDTO.setLiked(isLiked);
        }
    }

    @Override
    @Transactional
    public void addPostComment(Long postId,Comment comment) {
        if(comment == null){
            throw new RuntimeException("参数为空！添加失败");
        }
        UserDTO currentUser = UserHolder.getCurrentUser();
        Long currentUserId = UserHolder.getCurrentUser().getId();

        //获得该帖子的作者id
        Post post = postMapper.getPostById(postId);
        Long userId = post.getUserId();

        //todo 有待优化成aop
        // 发送消息给消息队列
        Map<String,Object> prop = new HashMap<>();
        prop.put("entityId",postId);
        EventMessage eventMessage = eventMessageProducer.createEventMessage(EVENT_TYPE_COMMENT, currentUserId, userId, prop);
        eventMessageProducer.sendMessage(eventMessage);

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

}
