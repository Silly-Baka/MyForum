package MyForum.controller;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.pojo.Conversation;
import MyForum.pojo.Message;
import MyForum.service.MessageService;
import MyForum.util.CommonUtil;
import MyForum.util.MyForumConstant;
import cn.hutool.core.util.StrUtil;
import org.aopalliance.intercept.Joinpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            throw new RuntimeException("非法参数！访问失败");
        }
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<ConversationDTO> page = messageService.getConversationListByToId(userId,currentPage);

        model.addAttribute("page", page);

        return "site/letter";
    }

    @GetMapping("/letter/list/{conversationId}/{currentPage}")
    public String getLetterListByConversationId(@PathVariable("conversationId") Long conversationId,
                                                @PathVariable("currentPage") Integer currentPage,
                                                Model model){
        if(conversationId == null){
            throw new RuntimeException("非法参数！访问失败");
        }
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<ConversationDTO> page = messageService.getLetterListByConversationId(conversationId, currentPage);
        model.addAttribute("page",page);

        return "site/letter-detail";
    }

    @PostMapping("/letter")
    @ResponseBody
    public String sendLetter(@RequestParam("toUserName") String toUserName,
                             @RequestParam("content") String content){
        if(StrUtil.isBlank(toUserName)){
            return CommonUtil.getJsonString(403,"目标用户的名字不可为空");
        }
        if(StrUtil.isBlank(content)){
            return CommonUtil.getJsonString(403,"消息内容不可为空！");
        }

        messageService.sendLetter(toUserName, content);

        return CommonUtil.getJsonString(200,"私信发送成功！");
    }
}
