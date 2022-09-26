package MyForum.DTO;

import MyForum.pojo.Message;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Date: 2022/9/18
 * Time: 15:44
 *
 * @Author SillyBaka
 * Description：
 **/
@Data
@NoArgsConstructor
public class MessageDTO extends Message {
    /**
     * 消息发出者的信息
     */
    private UserDTO fromUser;
}
