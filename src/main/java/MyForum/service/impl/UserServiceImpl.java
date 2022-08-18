package MyForum.service.impl;

import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.mapper.UserMapper;
import MyForum.pojo.User;
import MyForum.service.UserService;
import MyForum.util.MailClient;
import MyForum.util.RandomUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static MyForum.util.MyForumConstant.*;

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
        password = RandomUtil.md5(password + realUser.getSalt());
        if(!password.equals(realUser.getPassword())){
            log.error("密码错误，登陆失败！");
            map.put("passwordMsg","密码错误！");
            return map;
        }
        // 校验验证码
        String realCode = (String) redisTemplate.opsForValue().get(LOGIN_CODE_KEY + session.getId());
        if(!verifyCode.equals(realCode)){
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
        redisTemplate.expire(key,LOGIN_TOKEN_EXPIRED_TIME);

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
        String salt = RandomUtil.generateUUID().substring(5);
        user.setSalt(salt);
        // 将密码使用MD5加密
        password = RandomUtil.md5(password + salt);
        user.setPassword(password);
        user.setType(0);
        user.setStatus(0);
        String activationCode = RandomUtil.generateUUID();
        user.setActivationCode(activationCode);
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",RandomUtil.randomInt(1000)));
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
}
