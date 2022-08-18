package MyForum.DTO;

import MyForum.pojo.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Date: 2022/8/9
 * Time: 21:01
 *
 * @Author SillyBaka
 * Description：
 **/
@Data
public class UserDTO{
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 头像链接
     */
    private String headerUrl;
}
