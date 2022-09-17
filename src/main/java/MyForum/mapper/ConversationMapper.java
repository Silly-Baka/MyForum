package MyForum.mapper;

import MyForum.pojo.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    Conversation selectConversationByUserId(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    /**
     * 定期或是手动更新会话表的所有私信总数
     */
    void updateConversationLetterCount();
    /**
     * 定期或是手动更新会话表的未读私信总数
     */
    void updateConversationNotReadLetterCount();

    void insertConversation(Conversation conversation);

    void updateConversation(Conversation conversation);
}
