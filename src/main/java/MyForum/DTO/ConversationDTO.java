package MyForum.DTO;

import MyForum.pojo.Conversation;
import MyForum.pojo.Message;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Date: 2022/9/1
 * Time: 19:57
 *
 * @Author SillyBaka
 * Description：会话类，用来保存用户之间的私信列表
 **/
@Data
public class ConversationDTO extends Conversation {
    /**
     * 发送人的信息
     */
    private UserDTO user1;
    /**
     * 收件人的信息
     */
    private UserDTO user2;
    /**
     * 最新消息的详细信息
     */
    private Message newestMessage;
    /**
     * 私信列表
     */
    private List<Message> messageList;
}
