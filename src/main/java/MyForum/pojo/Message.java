package MyForum.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Date: 2022/9/1
 * Time: 16:46
 *
 * @Author SillyBaka
 * Description：私信类
 **/
@Data
public class Message {
    /**
     * 唯一id
     */
    private Long id;
    /**
     * 发出的用户id
     */
    private Long fromId;
    /**
     * 接收的用户id
     */
    private Long toId;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 状态  0-未读  1-已读  2-删除
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 消息类型  0-私信  1-关注通知  2-点赞通知  3-评论通知
     */
    private Integer messageType;
    /**
     * 所对应实体的id  比如说 私信 ----> 对话  所以私信存放对话的id
     */
    private Long entityId;
}
