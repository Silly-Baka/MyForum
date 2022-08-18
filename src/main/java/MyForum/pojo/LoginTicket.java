package MyForum.pojo;

/**
 * Date: 2022/8/11
 * Time: 14:11
 *
 * @Author SillyBaka
 * Description：登陆凭证 后期可以优化为token + redis
 **/
public class LoginTicket {
    /**
     * 凭证id
     */
    private Integer id;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 随机token
     */
    private String token;

}
