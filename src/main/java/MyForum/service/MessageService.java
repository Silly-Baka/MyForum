package MyForum.service;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.MessageDTO;
import MyForum.DTO.Page;
import MyForum.pojo.Message;

import java.util.Map;

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

    /**
     * 获得目标用户的系统通知概要列表
     * @param userId 目标用户id
     * @return 概要列表 -- 包含各种类型通知的最新通知、以及各种类型通知的总条数、未读通知的条数
     */
    Map<String,Object> getNoticeMessageSummary(Long userId);

    /**
     * 获得目标用户指定类型的通知列表
     * @param toId 目标用户id
     * @param messageType 消息类型
     * @param currentPage 当前页号
     * @return 通知列表
     */
    Page<MessageDTO> getNoticeMessageList(Long toId, Integer messageType, Integer currentPage);

    Long getUnreadCountByUserId(Long userId);
}
