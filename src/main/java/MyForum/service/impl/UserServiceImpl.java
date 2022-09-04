package MyForum.service.impl;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.mapper.UserMapper;
import MyForum.pojo.User;
import MyForum.service.UserService;
import MyForum.util.MailClient;
import MyForum.util.CommonUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static MyForum.util.MyForumConstant.*;
import static MyForum.util.RedisConstant.*;
/**
 * Date: 2022/8/9
 * Time: 20:52
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TemplateEngine engine;
    @Autowired
    private MailClient mailClient;

    @Value("${MyForum.path.domain}")
    private String domain;

    @Value("${MyForum.path.user.headerImagePath}")
    private String headerImagePath;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public Map<String, Object> login(String username, String password, String verifyCode, HttpSession session) {
        Map<String,Object> map = new HashMap<>();
        if(StrUtil.isBlank(username)){
            log.error("传入的用户名为空");
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StrUtil.isBlank(password)){
            log.error("传入的用户密码为空");
            map.put("passwordMsg","用户密码不能为空");
            return map;
        }
        if(StrUtil.isBlank(verifyCode)){
            log.error("传入的验证码为空");
            map.put("verifyCodeMsg","验证码不能为空");
            return map;
        }
        // 根据用户名寻找用户信息
        User realUser = userMapper.selectUserByUsername(username);
        if(realUser == null){
            log.error("不存在该用户，登陆失败");
            map.put("usernameMsg","不存在该用户，登陆失败");
            return map;
        }
        // 校验密码
        password = CommonUtil.md5(password + realUser.getSalt());
        if(!password.equals(realUser.getPassword())){
            log.error("密码错误，登陆失败！");
            map.put("passwordMsg","密码错误！");
            return map;
        }
        // 校验验证码
        String realCode = (String) redisTemplate.opsForValue().get(LOGIN_CODE_KEY + session.getId());
        if(!StrUtil.equalsIgnoreCase(verifyCode,realCode)){
            log.error("验证码错误");
            map.put("verifyCodeMsg","验证码错误，请重新获取");
            return map;
        }
        // 登陆成功后生成token并存放在session（一般都存放在前端的LocalStorage中,并将用户信息存入redis 方便获取
        String token = UUID.randomUUID().toString();

        //todo  使用session模拟前端保存token
        session.setAttribute("token",token);

        //todo  将用户信息存放在redis中
        String key = LOGIN_TOKEN_KEY + token;
        redisTemplate.opsForValue().set(key,realUser);
        redisTemplate.expire(key,LOGIN_TOKEN_EXPIRED_TIME, TimeUnit.MINUTES);

        //todo 使用BeanUtil将用户信息转换为DTO
        UserDTO user = BeanUtil.copyProperties(realUser, UserDTO.class);
        //todo 将用户信息DTO存放在ThreadLocal中 方便获取
        UserHolder.addUser(user);

        map.put("successMsg","登陆成功！");
        return map;
    }

    @Override
    public Map<String, Object> register(User user,String confirmPassword) {
        if(user == null){
            throw new IllegalArgumentException("用户不能为空！");
        }
        Map<String, Object> map = new HashMap<>();
        String username = user.getUsername();
        if(StrUtil.isBlank(username)){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        String password = user.getPassword();
        if(StrUtil.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StrUtil.isBlank(confirmPassword)){
            map.put("confirmPasswordMsg","确认密码不能为空!");
            return map;
        }
        if(!password.equals(confirmPassword)){
            map.put("confirmPasswordMsg","两次密码不一致！");
            return map;
        }
        String email = user.getEmail();
        if(StrUtil.isBlank(email)){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        // 检测是否有同一邮箱的用户
        User anotherUser = userMapper.selectUserByEmail(email);
        if(anotherUser != null){
            map.put("emailMsg","该邮箱已注册！请更换邮箱");
            return map;
        }
        String salt = CommonUtil.generateUUID().substring(5);
        user.setSalt(salt);
        // 将密码使用MD5加密
        password = CommonUtil.md5(password + salt);
        user.setPassword(password);
        user.setType(0);
        user.setStatus(0);
        String activationCode = CommonUtil.generateUUID();
        user.setActivationCode(activationCode);
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", CommonUtil.randomInt(1000)));
        user.setCreateTime(LocalDateTime.now());
        // 插入数据库
        userMapper.addUser(user);
        // 发送激活邮件给用户
        Context context = new Context();
        // http://localhost:8080/activation/userId/code
        String url = domain + "/activation/" + user.getId() + "/" + activationCode;

        context.setVariable("email",email);
        context.setVariable("url",url);

        String process = engine.process("/mail/activation", context);

        // 可以优化成立即返回 使用子线程来处理邮件发送
        mailClient.sendMail(email,"验证邮件",process);

        map.put("success","验证邮件已发送到您的邮箱！请注意查收并验证");
        return map;
    }

    @Override
    public Integer activation(Long userId, String activationCode) {
        User user = userMapper.selectUserById(userId);
        if(user == null){
            log.error("该用户id对应的用户不存在！");
            throw new RuntimeException("该用户id对应的用户不存在！");
        }
        String realCode = user.getActivationCode();
        // 激活码相同 激活成功
        if(StrUtil.isNotEmpty(activationCode) && realCode.equals(activationCode)){
            user.setStatus(1);
            userMapper.updateUserDynamic(user);

            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAIL;
    }

    @Override
    public Integer logout(String token) {
        if(StrUtil.isBlank(token)){
            return USER_LOGOUT_FAIL;
        }
        String key = LOGIN_TOKEN_KEY + token;
        redisTemplate.delete(key);

        return USER_LOGOUT_SUCCESS;
    }

    @Override
    public Map<String,Object> forget(String email, String verifyCode, String password) {
        Map<String,Object> map = new HashMap<>();
        if(StrUtil.isBlank(email)){
            map.put("emailMsg","输入的邮箱不能为空！");
            return map;
        }
        if(StrUtil.isBlank(verifyCode)){
            map.put("verifyCodeMsg","输入的验证码不能为空！");
            return map;
        }
        if(StrUtil.isBlank(password)){
            map.put("passwordMsg","输入的新密码不能为空！");
            return map;
        }

        String key = FORGET_CODE_KEY + email;
        String realCode = (String) redisTemplate.opsForValue().get(key);
        if(StrUtil.isBlank(realCode)){
            map.put("verifyCodeMsg","尚未获取验证码！请获取后再重新重置");
            return map;
        }
        if(!StrUtil.equalsIgnoreCase(verifyCode,realCode)){
            map.put("verifyCodeMsg","验证码错误！请重新检查后再次输入验证码");
            return map;
        }
        // 先通过邮箱从数据库中获取用户id信息
        User user = userMapper.selectUserByEmail(email);
        if(user == null){
            map.put("emailMsg","该邮箱尚未注册用户，或者用户不存在！请重新输入邮箱");
            return map;
        }
        // 再更改密码 再存进数据库
        password = CommonUtil.md5(password + user.getSalt());
        user.setPassword(password);
        userMapper.updateUserDynamic(user);

        map.put("success",1);

        return map;
    }

    @Override
    public Map<String,String> uploadHeaderImage(UserDTO user, MultipartFile headerImage, String token) {
        Map<String,String> map = new HashMap<>();
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 后缀为空 则文件格式不正确
        if(StrUtil.isBlank(suffix)){
            map.put("error","上传的文件格式不正确！");
            return map;
        }
        String newFileName = CommonUtil.generateUUID() + suffix;
        // http://localhost:8080/user/header/filename
        String headerUrl = domain + "/user/header/" + newFileName;

        // 更新用户的头像url
        User newUser = BeanUtil.copyProperties(user, User.class);
        newUser.setHeaderUrl(headerUrl);
        userMapper.updateUserDynamic(newUser);

        // 更新Redis中的token用户信息
        String key = LOGIN_TOKEN_KEY + token;
        redisTemplate.opsForValue().set(key,newUser);

        // 将头像文件存放进服务器本地
        try {
            String filePath = headerImagePath + newFileName;
            headerImage.transferTo(new File(filePath));
        } catch (IOException e) {
            log.debug("上传更新用户头像失败！");
            throw new RuntimeException("上传文件失败！服务器发生异常");
        }

        map.put("success","1");
        return map;
    }

    @Override
    public void getHeaderImage(String filename, HttpServletResponse response) {
        String filePath = headerImagePath + filename;
        String suffix = filename.substring(filename.lastIndexOf(".")+1);
        response.setContentType("image/"+suffix);

        try(FileInputStream fis = new FileInputStream(filePath)){

            ServletOutputStream os = response.getOutputStream();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fis.read(bytes)) != -1){
                os.write(bytes,0,len);
            }

        } catch (IOException e) {
            log.debug("获取头像图片失败！");
            throw new RuntimeException("获取头像图片失败，服务器发生异常！");
        }
    }

    @Override
    public Map<String, Object> changePassword(String oldPassword, String newPassword, String confirmPassword) {
        Map<String,Object> map = new HashMap<>();
        if(StrUtil.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原密码输入不能为空");
            return map;
        }
        if(StrUtil.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码输入不能为空");
            return map;
        }
        if(StrUtil.isBlank(confirmPassword)){
            map.put("confirmPasswordMsg","确认密码输入不能为空");
            return map;
        }
        UserDTO currentUser = UserHolder.getCurrentUser();

        User user = userMapper.selectUserById(currentUser.getId());
        if(user == null){
            log.error("服务器信息错误，查询不到此登录用户");
            throw new RuntimeException("服务器信息错误，查询不到此登录用户");
        }
        String password = user.getPassword();
        // 将密码经过MD5加密
        oldPassword = CommonUtil.md5(oldPassword + user.getSalt());
        newPassword = CommonUtil.md5(newPassword + user.getSalt());
        confirmPassword = CommonUtil.md5(confirmPassword + user.getSalt());
        // 验证原密码
        if(!oldPassword.equals(password)){
            map.put("oldPasswordMsg","原密码错误，请重新输入");
            return map;
        }
        // 验证新密码
        if(!newPassword.equals(confirmPassword)){
            map.put("confirmPasswordMsg","请重新确认密码，两次的输入不一致");
            return map;
        }
        // 更改密码 保存到数据库中
        User newUser = new User(user.getId());
        newUser.setPassword(newPassword);

        userMapper.updateUserDynamic(newUser);
        map.put("success","1");

        return map;
    }
}
