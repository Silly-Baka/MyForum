package com.example.myforum;

import MyForum.MyForumApplication;
import MyForum.mapper.PostMapper;
import MyForum.mapper.elasticsearch.PostESMapper;
import MyForum.pojo.Post;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2022/10/2
 * Time: 20:56
 *
 * @Author SillyBaka
 * Description：将数据从db刷新到es的测试类
 **/
@SpringBootTest(classes = MyForumApplication.class)
public class refreshElasticsearch {
    @Resource
    private PostESMapper postESMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void refreshData(){
        List<Post> allPost = postMapper.getAllPost();

        postESMapper.saveAll(allPost);
    }
}
