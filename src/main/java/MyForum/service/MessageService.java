package MyForum.service;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.Page;
import MyForum.pojo.Message;

/**
 * Date: 2022/9/1
 * Time: 20:09
 *
 * @Author SillyBaka
 * Description：
 **/
public interface MessageService {
    /**
     * 根据收件人的id获得所有私信
     * @param toId 收件人信息
     * @param currentPage 当前页号
     * @return 会话列表
     */
    Page<ConversationDTO> getConversationListByToId(Long toId, Integer currentPage);

    Page<ConversationDTO> getLetterListByConversationId(Long conversationId, Integer currentPage);

    /**
     * 发送私信
     * @param toUserName 目标用户的名字
     * @param content 私信内容
     */
    boolean sendLetter(String toUserName, String content);

    /**
     * 将消息保存到数据库中
     * @param message 消息对象
     */
    void addMessage(Message message);
}
