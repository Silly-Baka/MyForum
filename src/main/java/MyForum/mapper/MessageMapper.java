package MyForum.mapper;

import MyForum.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Date: 2022/9/1
 * Time: 17:20
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface MessageMapper {
    /**
     * 根据收件人的id获得所有向它发送私信的用户的id
     * @param toId 发送人的id
     * @return 所有发送私信的用户id
     */
    List<Long> selectFromIdListByToId(@Param("toId") Long toId);

    List<Message> selectMessageListByFromIdAndToId(@Param("fromId") Long fromId, @Param("toId") Long toId);

    Message selectMessageById(Long id);

    List<Message> selectMessageListByTypeAndEntityId(@Param("type") Integer type, @Param("entityId") Long entityId,
                                                     @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    Integer countMessageByEntityId(@Param("entityId") Long entityId);

    Integer addMessage(Message message);

    Message selectLatestMessageByToIdAndType(@Param("toId") Long toId,@Param("messageType") Integer messageType);

    Integer countMessageByToIdAndType(@Param("toId") Long toId,@Param("messageType") Integer messageType);

    List<Message> selectMessageListByToIdAndType(@Param("toId") Long toId, @Param("messageType") Integer messageType,
                                                 @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
}
