package MyForum.interceptor;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.service.MessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Date: 2022/9/27
 * Time: 19:38
 *
 * @Author SillyBaka
 * Description：用于获得未读消息数的拦截器
 **/
@Component
public class UnreadCountInterceptor implements HandlerInterceptor {

    @Resource
    private MessageService messageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO currentUser = UserHolder.getCurrentUser();
        if(currentUser == null){
            return true;
        }
        long unreadCount = messageService.getUnreadCountByUserId(currentUser.getId());
        request.setAttribute("unreadCount",unreadCount);

        return true;
    }
}
