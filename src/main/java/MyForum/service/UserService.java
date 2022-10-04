package MyForum.service;

import MyForum.DTO.UserDTO;
import MyForum.pojo.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
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

    /**
     * 登出用户
     * @param token 前端给的登录凭证
     * @return 返回登出结果
     */
    Integer logout(String token);

    /**
     * 忘记密码功能，用于忘记密码后修改密码
     * @param email 邮箱
     * @param verifyCode 验证码
     * @param password 新密码
     * @return 操作结果
     */
    Map<String,Object> forget(String email,String verifyCode,String password);

    /**
     * 上传头像 并更改指定用户的头像
     * @param user 指定用户
     * @param headerImage 新头像
     * @param token 登陆凭证
     * @return 返回操作结果
     */
    Map<String,String> uploadHeaderImage(UserDTO user, MultipartFile headerImage, String token);

    /**
     * 根据图片名字从服务器本地获取头像 并响应回客户端
     * @param filename 图片名
     * @param response 响应对象
     */
    void getHeaderImage(String filename, HttpServletResponse response);

    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @return 操作结果
     */
    Map<String,Object> changePassword(String oldPassword,String newPassword,String confirmPassword);

    /**
     * 获取用户首页的信息
     * @param userId 用户id
     * @return 用户首页信息
     */
    UserDTO getUserProfile(Long userId);

    /**
     * 根据用户id获得该用户的权限
     * @param userId 用户id
     * @return 用户权限级别
     */
    Collection<? extends GrantedAuthority> getUserAuthority(Long userId);
}
