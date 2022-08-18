package MyForum.common;

import MyForum.DTO.UserDTO;
import MyForum.pojo.User;

/**
 * Date: 2022/8/17
 * Time: 16:09
 *
 * @Author SillyBaka
 *
 * Description：用于线程内存放、获取用户登陆信息
 **/
public class UserHolder {
    private UserHolder(){}

    private static final ThreadLocal<UserDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void addUser(UserDTO user){
        USER_THREAD_LOCAL.set(user);
    }

    public static UserDTO getCurrentUser(){
        return USER_THREAD_LOCAL.get();
    }

    public static void removeUser(){
        USER_THREAD_LOCAL.remove();
    }
}
