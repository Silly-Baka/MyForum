package MyForum.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Date: 2022/8/9
 * Time: 20:10
 *
 * @Author SillyBaka
 * Description：
 **/
@Data
@NoArgsConstructor
public class User implements Serializable {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户名字
     */
    private String username;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 加盐（一个随机的字符串 用于加密）
     */
    private String salt;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户类型
     * 0-普通用户; 1-管理员; 2-版主
     */
    private Integer type;
    /**
     * 用户是否已激活
     * 0-未激活; 1-已激活;
     */
    private Integer status;
    /**
     * 验证码
     */
    private String activationCode;
    /**
     * 头像链接
     */
    private String headerUrl;

    /**
     * 用户创建时间
     */
    private LocalDateTime createTime;
}
