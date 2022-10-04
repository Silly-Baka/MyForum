package MyForum.service.impl;

import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.mapper.UserMapper;
import MyForum.mapper.elasticsearch.PostESMapper;
import MyForum.pojo.Post;
import MyForum.pojo.User;
import MyForum.service.SearchService;
import cn.hutool.core.bean.BeanUtil;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Date: 2022/10/2
 * Time: 15:52
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private PostESMapper postESMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<Post> searchPost(String keyWord, int currentPage, int pageSize) {

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 帖子标题、内容
        NativeSearchQuery query = queryBuilder.withQuery(new MultiMatchQueryBuilder(keyWord,"title","content")
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                )
                // 排除被撤销的帖子
                .withFilter(new RangeQueryBuilder("status")
                        .lt(2))
                .withSorts(
                        // 按照类型降序
                        SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        // 按照是否是精品来排序
                        SortBuilders.fieldSort("status").order(SortOrder.DESC),
                        // 按照时间降序
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC),
                        // 按照内部评分机制降序
                        SortBuilders.scoreSort().order(SortOrder.DESC)
                )
                // 分页
                .withPageable(Pageable.ofSize(pageSize).withPage(currentPage))
                // 高亮标题中的关键词
                .withHighlightBuilder(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>")
//                // 高亮帖子内容中的关键词
                        .field("content").preTags("<em>").postTags("</em>"))
                .build();

        SearchHits<Post> queryResult = elasticsearchRestTemplate.search(query,Post.class);

        Page<Post> page = new Page<>(currentPage,(int) queryResult.getTotalHits());

        List<Post> postList = new ArrayList<>();

        for (SearchHit<Post> searchHit : queryResult.getSearchHits()) {
            List<String> title = searchHit.getHighlightField("title");
            List<String> content = searchHit.getHighlightField("content");

            Post post = searchHit.getContent();
            // 补充用户信息
            User user = userMapper.selectUserById(post.getUserId());
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);

            post.setUser(userDTO);
            if(title.size() > 0){
                post.setTitle(title.get(0));
            }
            if(content.size() > 0){
                post.setContent(content.get(0));
            }
            postList.add(post);
        }

        page.setRecords(postList);

        return page;
    }
}
