package MyForum.mapper;

import MyForum.DTO.UserDTO;
import MyForum.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Date: 2022/8/9
 * Time: 21:12
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface UserMapper {
    /**
     * 根据邮箱获取用户
     * @param email 邮箱
     * @return 用户信息
     */
    User selectUserByEmail(@Param("email") String email);

    /**
     * 注册用户
     * @param user 用户信息
     * @return 用户id
     */
    Integer addUser(User user);

    /**
     * 根据id获取用户
     */
    User selectUserById(@Param("id") Long id);

    /**
     * 动态更新用户信息，根据传入的user带有的属性值来更新
     * @param user 用户信息
     */
    Integer updateUserDynamic(User user);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    User selectUserByUsername(@Param("username") String username);

    /**
     * 根据用户id获得用户的dto对象
     * @param userId 用户id
     * @return
     */
    User selectUserDTOByUserId(@Param("userId") Long userId);
}
