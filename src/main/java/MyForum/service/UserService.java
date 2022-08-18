package MyForum.service;

import MyForum.DTO.UserDTO;
import MyForum.pojo.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Date: 2022/8/9
 * Time: 20:52
 *
 * @Author SillyBaka
 * Description：
 **/
public interface UserService {

    /**
     * 登陆
     * @return 登陆结果
     */
    Map<String,Object> login(String username, String password, String verifyCode, HttpSession session);

    /**
     * 注册用户
     * @param user 用户信息
     * @return 返回注册结果
     */
    Map<String,Object> register(User user,String confirmPassword);

    /**
     * 激活用户
     * @param userId 用户id
     * @param activationCode 激活码
     * @return 返回激活结果
     */
    Integer activation(Long userId,String activationCode);
}
