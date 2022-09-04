package MyForum.service.impl;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.mapper.ConversationMapper;
import MyForum.mapper.MessageMapper;
import MyForum.mapper.UserMapper;
import MyForum.pojo.Conversation;
import MyForum.pojo.Message;
import MyForum.pojo.User;
import MyForum.service.MessageService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
}
