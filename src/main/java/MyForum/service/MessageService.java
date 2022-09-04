package MyForum.service;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.Page;

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
}
