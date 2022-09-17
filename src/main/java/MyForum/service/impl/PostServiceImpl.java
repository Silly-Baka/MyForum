package MyForum.service.impl;

import MyForum.DTO.UserDTO;
import MyForum.common.Config;
import MyForum.common.UserHolder;
import MyForum.mapper.PostMapper;
import MyForum.DTO.Page;
import MyForum.mapper.UserMapper;
import MyForum.pojo.Post;
import MyForum.pojo.User;
import MyForum.service.PostService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static MyForum.redis.RedisConstant.*;

/**
 * Date: 2022/8/8
 * Time: 12:44
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public Page<Post> getPostList(Integer currentPage) {
        // 避免页号出现0的情况
        if(currentPage < 0){
            currentPage = 1;
        }
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
            String key = LIKE_POST_KEY + post.getId();
            Long likeCount = redisTemplate.opsForSet().size(key);
            if(likeCount != null){
                post.setScore(likeCount.intValue());
            }
        }
        page.setRecords(postList);

        return page;
    }

    @Override
    public Page<Post> getHotPosts(Integer currentPage) {
        // 避免页号出现0的情况
        if(currentPage < 0){
            currentPage = 0;
        }
        Integer totalCount = postMapper.getCount(null);
        Page<Post> page = new Page<>(currentPage, totalCount);

        List<Post> postList = postMapper.getHotPosts((currentPage-1)*Config.getPageSize(),page.getPageSize());
        // 将user信息包装进每一个postDetaill里面
        // 批量查询会不会提高效率？
        for (Post post : postList) {
            Long userId = post.getUserId();
            User user = userMapper.selectUserById(userId);
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            post.setUser(userDTO);

            // 从redis中查帖子的点赞数 存入帖子
            String key = LIKE_POST_KEY + post.getId();
            Long likeCount = redisTemplate.opsForSet().size(key);
            if(likeCount != null){
                post.setScore(likeCount.intValue());
            }
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
    }

    @Override
    public Post getPostById(Long postId) {
        // 先到redis中查是否有缓存
        String key = CACHE_POST_KEY + postId;
        Post post = (Post) redisTemplate.opsForValue().get(key);

        // 查询当前用户是否有点赞该帖子
        UserDTO currentUser = UserHolder.getCurrentUser();
        Boolean isLiked = redisTemplate.opsForSet().isMember(LIKE_POST_KEY + postId, currentUser.getId());

        String countKey = LIKE_POST_KEY + postId;
        // 从redis中查帖子的点赞数 存入帖子
        Long likeCount = redisTemplate.opsForSet().size(countKey);

        if(post != null){
            post.setIsLiked(isLiked);

            if(likeCount != null){
                post.setScore(likeCount.intValue());
            }else {
                post.setScore(0);
            }
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

        if(likeCount != null){
            post.setScore(likeCount.intValue());
        }else {
            post.setScore(0);
        }
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
            String countKey = LIKE_POST_KEY + post.getId();
            Long likeCount = redisTemplate.opsForSet().size(countKey);
            if(likeCount != null){
                post.setScore(likeCount.intValue());
            }
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
        String userLikeCountKey = LIKE_USER_TOTAL_KEY+post.getUserId();

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

        Integer likeCount = postMapper.getScoreByPostId(postId);
        map.put("likeCount",likeCount);

        return map;
    }
}
