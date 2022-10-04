package MyForum.service.impl;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.MessageDTO;
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
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static MyForum.util.MyForumConstant.*;

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

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ConversationMapper conversationMapper;
    @Resource
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
    @Transactional
    public Page<ConversationDTO> getLetterListByConversationId(Long conversationId, Integer currentPage) {

        Conversation conversation = conversationMapper.selectConversationById(conversationId);

        Page<ConversationDTO> page = new Page<>(currentPage, conversation.getLetterCount());

        List<Message> letterList = messageMapper.selectMessageListByTypeAndEntityId(MESSAGE_TYPE_LETTER, conversationId,
                (currentPage - 1) * page.getPageSize(), page.getPageSize());

        // 更新db 将这些消息设为已读
        List<Long> ids = new ArrayList<>();
        letterList.forEach((message -> {
            ids.add(message.getId());
        }));
        ids.forEach((id->{
            messageMapper.updateMessageStatusByIds(id,1);
        }));

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

    @Override
    public Map<String, Object> getNoticeMessageSummary(Long userId) {
        Map<String,Object> map = new HashMap<>();

        for (Integer messageType : NOTICE_TYPE_SET) {
            String typeName = MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME.get(messageType);
            // 获取所有类型的最新通知
            Message message = messageMapper.selectLatestMessageByToIdAndType(userId, messageType);
            map.put(typeName+"Message",message);
            // 获取该类型通知的条数
            Integer messageCount = messageMapper.countMessageByToIdAndType(userId, messageType);
            if(messageCount == null){
                messageCount = 0;
            }
            map.put(typeName+"Count",messageCount);

            if(message != null){
                // 获取通知发起者的名字
                Long fromId = message.getFromId();
                User fromUser = userMapper.selectUserById(fromId);

                map.put(typeName+"Username",fromUser.getUsername());
            }
        }

        return map;
    }

    @Override
    public Page<MessageDTO> getNoticeMessageList(Long toId, Integer messageType, Integer currentPage) {
        // 获得消息的总条数
        Integer count = messageMapper.countMessageByToIdAndType(toId, messageType);
        // 获取分页对象
        Page<MessageDTO> page = new Page<>(currentPage,count);

        List<Message> messageList = messageMapper.selectMessageListByToIdAndType(toId, messageType, (currentPage - 1) * page.getPageSize(), page.getPageSize());
        List<MessageDTO> messageDTOList = new ArrayList<>();

        // 记录消息的id 等待更改为已读
        List<Long> ids = new ArrayList<>();

        for(Message message:messageList){
            MessageDTO messageDTO = BeanUtil.copyProperties(message, MessageDTO.class);
            // 获取消息发出者的消息
            User fromUser = userMapper.selectUserById(message.getFromId());
            UserDTO fromUserDTO = BeanUtil.copyProperties(fromUser, UserDTO.class);

            messageDTO.setFromUser(fromUserDTO);

            messageDTOList.add(messageDTO);

            ids.add(message.getId());
        }

        page.setRecords(messageDTOList);

        ids.forEach(id->{
            messageMapper.updateMessageStatusByIds(id,1);
        });

        return page;
    }

    @Override
    public Long getUnreadCountByUserId(Long userId) {
        return messageMapper.selectUnreadMessageCountByUserId(userId);
    }
}
