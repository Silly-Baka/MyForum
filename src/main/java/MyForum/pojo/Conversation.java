package MyForum.pojo;

import MyForum.DTO.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Date: 2022/9/1
 * Time: 22:58
 *
 * @Author SillyBaka
 * Description：会话类
 **/
@Data
public class Conversation {
    /**
     * 唯一id
     */
    private Long id;
    /**
     * 用户1的信息
     */
    private Long user1Id;
    /**
     * 用户2的信息
     */
    private Long user2Id;
    /**
     * 会话消息条数
     */
    private Integer letterCount;
    /**
     * 最新消息的id
     */
    private Long newestMessageId;
}
