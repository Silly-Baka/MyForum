package MyForum.mapper.elasticsearch;

import MyForum.pojo.Post;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Date: 2022/10/2
 * Time: 15:46
 *
 * @Author SillyBaka
 * Description：用于在es中查询帖子消息的mapper
 **/
@Repository
public interface PostESMapper extends ElasticsearchRepository<Post,Long> {
}
