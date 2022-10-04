package MyForum.controller;

import MyForum.DTO.ConversationDTO;
import MyForum.DTO.MessageDTO;
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

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static MyForum.util.MyForumConstant.MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME;

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

    @Resource
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

    // 获得当前用户的系统通知概要
    @GetMapping("/notice/summary")
    public String getNoticeMessageSummary(Model model){
        UserDTO currentUser = UserHolder.getCurrentUser();
        if(currentUser == null){
            throw new RuntimeException("尚未登录，无法获取系统通知列表！");
        }
        Map<String,Object> map = messageService.getNoticeMessageSummary(currentUser.getId());
        model.addAttribute("map",map);

        return "site/notice";
    }

    @GetMapping("/notice/{messageType}/list/{currentPage}")
    public String getNoticeMessageList(@PathVariable("messageType") Integer messageType,
                                       @PathVariable("currentPage") Integer currentPage,
                                       Model model){
        if(messageType == null){
            throw new RuntimeException("参数错误，无法查询");
        }
        UserDTO currentUser = UserHolder.getCurrentUser();
        if(currentUser == null){
            throw new RuntimeException("尚未登录，无法获得通知列表");
        }

        Page<MessageDTO> page = messageService.getNoticeMessageList(currentUser.getId(), messageType, currentPage);

        String typeName = MESSAGE_TYPE_TO_MESSAGE_TYPE_NAME.get(messageType);
        // 暂存messageType到客户端 方便复用
        model.addAttribute("messageType",messageType);

        model.addAttribute("page",page);

        model.addAttribute("typeName",typeName);

        return "site/notice-detail";
    }
}
