package MyForum.service.impl;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.mapper.ConversationMapper;
import MyForum.mapper.MessageMapper;
import MyForum.mapper.UserMapper;
import MyForum.pojo.Conversation;
import MyForum.pojo.Message;
import MyForum.pojo.User;
import MyForum.service.MessageService;
import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static MyForum.util.MyForumConstant.MESSAGE_TYPE_LETTER;

/**
 * Date: 2022/9/1
 * Time: 20:19
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public Page<ConversationDTO> getConversationListByToId(Long toId, Integer currentPage) {

        Integer count = conversationMapper.selectCountByUserId(toId);

        Page<ConversationDTO> page = new Page<>(currentPage,count);

        List<Conversation> conversationList = conversationMapper.selectConversationListByUserId(toId,(currentPage-1)*page.getPageSize(),page.getPageSize());
        List<ConversationDTO> conversationDTOList = new ArrayList<>();

        for (Conversation conversation : conversationList) {
            // 往里面填充用户信息
            Long user1Id = conversation.getUser1Id();
            Long user2Id = conversation.getUser2Id();
            User user1 = userMapper.selectUserDTOByUserId(user1Id);
            User user2 = userMapper.selectUserDTOByUserId(user2Id);

            ConversationDTO conversationDTO = BeanUtil.copyProperties(conversation, ConversationDTO.class);
            conversationDTO.setUser1(BeanUtil.copyProperties(user1,UserDTO.class));
            conversationDTO.setUser2(BeanUtil.copyProperties(user2,UserDTO.class));

            // 填充最新消息的信息
            Long newestMessageId = conversation.getNewestMessageId();
            if(newestMessageId != null){
                conversationDTO.setNewestMessage(messageMapper.selectMessageById(newestMessageId));
            }

            conversationDTOList.add(conversationDTO);
        }

        page.setRecords(conversationDTOList);

        return page;
    }

    @Override
    public Page<ConversationDTO> getLetterListByConversationId(Long conversationId, Integer currentPage) {

        Conversation conversation = conversationMapper.selectConversationById(conversationId);

        Page<ConversationDTO> page = new Page<>(currentPage, conversation.getLetterCount());

        List<Message> letterList = messageMapper.selectMessageListByTypeAndEntityId(MESSAGE_TYPE_LETTER, conversationId,
                (currentPage - 1) * page.getPageSize(), page.getPageSize());

        ConversationDTO conversationDTO = BeanUtil.copyProperties(conversation, ConversationDTO.class);
        conversationDTO.setMessageList(letterList);

        User user1 = userMapper.selectUserById(conversationDTO.getUser1Id());
        conversationDTO.setUser1(BeanUtil.copyProperties(user1,UserDTO.class));

        User user2 = userMapper.selectUserById(conversationDTO.getUser2Id());
        conversationDTO.setUser2(BeanUtil.copyProperties(user2,UserDTO.class));

        page.setRecords(Collections.singletonList(conversationDTO));
        return page;
    }

    @Override
    @Transactional
    public boolean sendLetter(String toUserName, String content) {
        UserDTO fromUser = UserHolder.getCurrentUser();

        User toUser = userMapper.selectUserByUsername(toUserName);

        // 目标用户不存在
        if(toUser == null){
            return false;
        }
        // 先查找两个用户是否曾存在对话
        Conversation conversation = conversationMapper.selectConversationByUserId(fromUser.getId(), toUser.getId());
        // 若不存在 则需创建一个新的对话 并存入数据库
        if(conversation == null){
            conversation = new Conversation();
            conversation.setUser1Id(fromUser.getId());
            conversation.setUser2Id(toUser.getId());
            conversation.setLetterCount(0);
            conversation.setNotReadLetterCount(0);
            conversationMapper.insertConversation(conversation);
        }
        Message message = new Message();
        message.setFromId(fromUser.getId());
        message.setToId(toUser.getId());
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        message.setMessageType(MESSAGE_TYPE_LETTER);
        message.setEntityId(conversation.getId());
        message.setStatus(0);

        messageMapper.addMessage(message);

        // 加入私信成功 更新会话
        conversation.setLetterCount(conversation.getLetterCount()+1);
        conversation.setNewestMessageId(message.getId());
        conversation.setNotReadLetterCount(conversation.getNotReadLetterCount()+1);
        conversationMapper.updateConversation(conversation);

        return true;
    }

    @Override
    public void addMessage(Message message) {
        messageMapper.addMessage(message);
    }
}
