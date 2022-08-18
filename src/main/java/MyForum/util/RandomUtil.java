package MyForum.util;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

import java.util.Random;
import java.util.UUID;

/**
 * Date: 2022/8/9
 * Time: 20:39
 *
 * @Author SillyBaka
 * Description：用于获取随机唯一字符串或是MD5加密的工具类
 **/
public class RandomUtil {

    private static final Random RANDOM = new Random();
    /**
     * 获取一个唯一的随机字符串
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * 对key进行MD5加密
     *
     * @param key 需要加密的密码
     * @return 经MD5加密后的字符串
     */
    public static String md5(String key){
        if(StrUtil.isBlank(key)){
            return null;
        }
        return SecureUtil.md5(key);
    }

    public static Integer randomInt(int bound){
        return RANDOM.nextInt(bound);
    }
}
