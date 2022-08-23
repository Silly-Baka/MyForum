package MyForum.service.impl;

import MyForum.DTO.UserDTO;
import MyForum.mapper.PostDetailMapper;
import MyForum.DTO.Page;
import MyForum.mapper.UserMapper;
import MyForum.pojo.PostDetail;
import MyForum.pojo.User;
import MyForum.service.PostDetailService;
import cn.hutool.core.bean.BeanUtil;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Date: 2022/8/8
 * Time: 12:44
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
public class PostDetailServiceImpl implements PostDetailService {
    @Autowired
    private PostDetailMapper postDetailMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<PostDetail> getPostList(Integer currentPage) {
        // 避免页号出现0的情况
        if(currentPage < 0){
            currentPage = 0;
        }
        Integer totalCount = postDetailMapper.getCount();
        Page<PostDetail> page = new Page<>(currentPage, totalCount);

        List<PostDetail> postList = postDetailMapper.getPostList(currentPage,page.getPageSize());
        // 将user信息包装进每一个postDetaill里面
        // 批量查询会不会提高效率？
        for (PostDetail postDetail : postList) {
            Long userId = postDetail.getUserId();
            User user = userMapper.selectUserById(userId);
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            postDetail.setUser(userDTO);
        }
        page.setRecords(postList);

        return page;
    }

    @Override
    public Page<PostDetail> getHotPosts(Integer currentPage) {
        // 避免页号出现0的情况
        if(currentPage < 0){
            currentPage = 0;
        }
        Integer totalCount = postDetailMapper.getCount();
        Page<PostDetail> page = new Page<>(currentPage, totalCount);

        List<PostDetail> postList = postDetailMapper.getHotPosts(currentPage,page.getPageSize());
        // 将user信息包装进每一个postDetaill里面
        // 批量查询会不会提高效率？
        for (PostDetail postDetail : postList) {
            Long userId = postDetail.getUserId();
            User user = userMapper.selectUserById(userId);
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            postDetail.setUser(userDTO);
        }
        page.setRecords(postList);

        return page;
    }
}
