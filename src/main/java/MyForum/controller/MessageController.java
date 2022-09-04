package MyForum.controller;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.pojo.Conversation;
import MyForum.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Date: 2022/9/1
 * Time: 19:36
 *
 * @Author SillyBaka
 * Description：
 **/
@Controller
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;


    /**
     * 获得目标用户的会话列表
     * @param userId 目标用户的id
     * @param currentPage 当前页号
     */
    @GetMapping("/conversation/list/{userId}/{currentPage}")
    public String getConversationList(@PathVariable("userId") Long userId,
                                 @PathVariable("currentPage") Integer currentPage,
                                 Model model){
        if(userId == null){
            return "";
        }
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<ConversationDTO> page = messageService.getConversationListByToId(userId,currentPage);

        model.addAttribute("userId",userId);
        model.addAttribute("page", page);

        return "site/letter";
    }

    @GetMapping("/letter/list/{conversationId}/{currentPage}")
    public String getLetterListByConversationId(@PathVariable("conversationId") Long conversationId,
                                                @PathVariable("currentPage") Integer currentPage,
                                                Model model){
        if(conversationId == null){
            return "";
        }
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<ConversationDTO> page = messageService.getLetterListByConversationId(conversationId, currentPage);
        model.addAttribute("page",page);

        return "site/letter-detail";
    }
}
