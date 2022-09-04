package MyForum.mapper;

import MyForum.pojo.Conversation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Date: 2022/9/2
 * Time: 15:51
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface ConversationMapper {
    List<Conversation> selectConversationListByUserId(Long userId,Integer offset,Integer pageSize);

    Conversation selectConversationById(Long id);

    Integer selectCountByUserId(Long userId);

    /**
     * 定期或是手动更新会话表的所有私信总数
     */
    void updateConversationLetterCount();
}
