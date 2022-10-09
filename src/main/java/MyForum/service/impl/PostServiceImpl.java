package MyForum.service.impl;

import MyForum.DTO.UserDTO;
import MyForum.config.Config;
import MyForum.common.UserHolder;
import MyForum.mapper.PostMapper;
import MyForum.DTO.Page;
import MyForum.mapper.UserMapper;
import MyForum.pojo.EventMessage;
import MyForum.pojo.Post;
import MyForum.pojo.User;
import MyForum.rabbitMQ.EventMessageProducer;
import MyForum.redis.RedisConstant;
import MyForum.service.PostService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static MyForum.redis.RedisConstant.*;
import static MyForum.util.MyForumConstant.*;

/**
 * Date: 2022/8/8
 * Time: 12:44
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
@Slf4j
public class PostServiceImpl implements PostService {
    @Resource
    private PostMapper postMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private EventMessageProducer eventMessageProducer;

    @Override
    public Page<Post> getPostList(Integer currentPage) {

        Integer totalCount = postMapper.getCount(null);
        Page<Post> page = new Page<>(currentPage, totalCount);

        List<Post> postList = postMapper.getPostList((currentPage-1)*Config.getPageSize(),page.getPageSize());
        // 将user信息包装进每一个postDetaill里面
        // 批量查询会不会提高效率？
        for (Post post : postList) {
            Long userId = post.getUserId();
            User user = userMapper.selectUserById(userId);
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            post.setUser(userDTO);

            // 从redis中查帖子的点赞数 存入帖子
            Integer likeCount = (Integer) redisTemplate.opsForValue().get(RedisConstant.getLikePostCountKey(post.getId()));
            likeCount = likeCount != null ? likeCount : 0;

            post.setLikeCount(Long.valueOf(likeCount));
        }
        page.setRecords(postList);

        return page;
    }

    @Override
    public Page<Post> getHotPosts(Integer currentPage) {

//        Integer totalCount = postMapper.getCount(null);
//        Page<Post> page = new Page<>(currentPage, totalCount);
//
//        List<Post> postList = postMapper.getHotPosts((currentPage-1)*Config.getPageSize(),page.getPageSize());
        Long size = redisTemplate.opsForZSet().size(HOTTEST_POST_KEY);
        if(size == null){
            log.error("服务端出现错误，无法查询到热帖列表");
            throw new RuntimeException("服务端出现错误，无法查询到热帖列表");
        }
        long start = (long) (currentPage - 1) *Config.getPageSize();
        long end = start + Config.getPageSize();

        Page<Post> page = new Page<>(currentPage, size.intValue());
        Set<Object> postIds = redisTemplate.opsForZSet().reverseRange(HOTTEST_POST_KEY, start, end-1);

        if(postIds == null){
            log.error("服务端出现错误，无法查询到热帖列表");
            throw new RuntimeException("服务端出现错误，无法查询到热帖列表");
        }

        List<Post> postList = new ArrayList<>();
        for (Object postId : postIds) {
            Post post = getPostById(Long.valueOf(String.valueOf(postId)));

            postList.add(post);
        }

        page.setRecords(postList);

        return page;
    }

    @Override
    public void publishPost(String title, String content) {

        Post post = new Post();
        UserDTO currentUser = UserHolder.getCurrentUser();

        post.setUserId(currentUser.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(LocalDateTime.now());

        postMapper.addPost(post);

        // 创建事件并发送给消息队列 -- 将新的数据同步到es
        EventMessage eventMessage = eventMessageProducer.createEventMessage(EVENT_TYPE_POST_ADD, null, post.getId(),
                Collections.singletonMap("post", post));
        eventMessageProducer.sendMessage(eventMessage);

    }

    @Override
    public Post getPostById(Long postId) {
        // 先到redis中查是否有缓存
        String key = CACHE_POST_KEY + postId;
        Post post = (Post) redisTemplate.opsForValue().get(key);

        // 查询当前用户是否有点赞该帖子
        UserDTO currentUser = UserHolder.getCurrentUser();

        Boolean isLiked = false;
        if(currentUser != null){
           isLiked = redisTemplate.opsForSet().isMember(LIKE_POST_KEY + postId, currentUser.getId());
        }


        if(post != null){
            post.setIsLiked(isLiked);

            // 从redis中查帖子的点赞数 存入帖子
            Integer likeCount = (Integer) redisTemplate.opsForValue().get(RedisConstant.getLikePostCountKey(post.getId()));
            likeCount = likeCount != null ? likeCount : 0;

            post.setLikeCount(Long.valueOf(likeCount));
            return post;
        }
        // 若没有缓存 则到数据库中查
        post = postMapper.getPostById(postId);

        if(post == null){
            throw new RuntimeException("服务器异常，无法查询到该id的帖子");
        }
        Long userId = post.getUserId();
        UserDTO userDTO = BeanUtil.copyProperties(userMapper.selectUserById(userId), UserDTO.class);
        post.setUser(userDTO);
        post.setIsLiked(isLiked);
        // 从redis中查帖子的点赞数 存入帖子
        Integer likeCount = (Integer) redisTemplate.opsForValue().get(RedisConstant.getLikePostCountKey(post.getId()));
        likeCount = likeCount != null ? likeCount : 0;

        post.setLikeCount(Long.valueOf(likeCount));

        // 查完后缓存到redis中
        redisTemplate.opsForValue().set(key,post,CACHE_POST_EXPIRED_TIME, TimeUnit.SECONDS);

        return post;
    }

    @Override
    public Page<Post> getPostListByUserId(Long userId, Integer currentPage) {
        String key = CACHE_POST_PAGE_KEY + userId + ":" + currentPage;
        Page<Post> page = (Page<Post>) redisTemplate.opsForValue().get(key);
        if(page != null){
            return page;
        }
        Integer total = postMapper.getCount(userId);
        page = new Page<>(currentPage, total);
        List<Post> postList = postMapper.getPostListByUserId(userId, (currentPage - 1) * page.getPageSize(), page.getPageSize());
        User user = userMapper.selectUserById(userId);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        for (Post post : postList) {
            post.setUser(userDTO);
            // 从redis中查帖子的点赞数 存入帖子
            Integer likeCount = (Integer) redisTemplate.opsForValue().get(RedisConstant.getLikePostCountKey(post.getId()));
            likeCount = likeCount != null ? likeCount : 0;

            post.setLikeCount(Long.valueOf(likeCount));
        }

        page.setRecords(postList);

        redisTemplate.opsForValue().set(key,page,CACHE_COMMENT_EXPIRED_TIME,TimeUnit.SECONDS);

        return page;
    }

    @Override
    @Transactional
    public Map<String,Object> likePost(Long postId) {
        UserDTO currentUser = UserHolder.getCurrentUser();
        Map<String,Object> map = new HashMap<>();

        String key = LIKE_POST_KEY + postId;

        // 根据帖子信息获取发布者的id
        Post post = postMapper.getPostById(postId);
        // 更新用户被点赞总数
        String userLikeCountKey = LIKE_USER_COUNT_KEY +post.getUserId();

        String userId = currentUser.getId().toString();
        // 通过redis查是否有本用户 来判断是否已点赞
        Boolean isLiked = redisTemplate.opsForSet().isMember(key, userId);

        // 如果已点赞 则取消点赞
        if(BooleanUtil.isTrue(isLiked)){
            // 数据库点赞-1
            Integer result = postMapper.incrScoreByPostId(postId, false);
            // 操作成功 redis取消点赞
            if(result != null){
               redisTemplate.opsForSet().remove(key,userId);
               // 用户总被点赞数-1
               redisTemplate.opsForValue().decrement(userLikeCountKey);
               map.put("likeStatus",0);
            }
        }else {
        // 未点赞 则点赞
            Integer result = postMapper.incrScoreByPostId(postId, true);
            if(result != null){
            // redis增加该用户的点赞
                redisTemplate.opsForSet().add(key,userId);
                // 用户被点赞总数+1
                redisTemplate.opsForValue().increment(userLikeCountKey);
                map.put("likeStatus",1);
            }
        }
        // 将帖子缓存删掉 防止数据不一致
        redisTemplate.delete(CACHE_POST_KEY+postId);

        // 从redis中查帖子的点赞数 存入帖子
        Integer likeCount = (Integer) redisTemplate.opsForValue().get(RedisConstant.getLikePostCountKey(post.getId()));
        likeCount = likeCount != null ? likeCount : 0;

        map.put("likeCount",likeCount);

        //todo 记录被点赞的帖子id
        redisTemplate.opsForSet().add(OPERATED_POST_KEY,postId);

        return map;
    }

    @Override
    public void setPostWonderful(Long postId, Integer status) {
        Post post = new Post();
        post.setId(postId);

        // 未加精 则加精
        if(status == 0){
            post.setStatus(1);

        }else if(status == 1){
        // 已加精 则取消加精
            post.setStatus(0);
        }

        postMapper.updatePostDynamic(post);

        // 删redis缓存
        redisTemplate.delete(CACHE_POST_KEY + postId);

        //todo 记录被加精的帖子id
        redisTemplate.opsForSet().add(OPERATED_POST_KEY,postId);
    }

    @Override
    public void setPostTop(Long postId, Integer type) {
        Post post = new Post();
        post.setId(postId);

        // 未置顶 则置顶
        if(type == 0){
            post.setType(1);
        }else{
        // 已置顶 则取消置顶
            post.setType(0);
        }

        postMapper.updatePostDynamic(post);
        // 删redis缓存
        redisTemplate.delete(CACHE_POST_KEY + postId);
    }

    @Override
    public void deletePost(Long postId) {
        postMapper.deletePost(postId);
        // 删redis缓存
        redisTemplate.delete(CACHE_POST_KEY + postId);
    }
}
