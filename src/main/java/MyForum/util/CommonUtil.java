package MyForum.util;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Date: 2022/8/9
 * Time: 20:39
 *
 * @Author SillyBaka
 * Description： 常用工具类
 **/
public class CommonUtil {

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

    /**
     * 用于获取统一的Json字符串，便于与前端通信
     * @param code http状态码
     * @param msg 错误信息
     * @param data 数据
     * @return 返回统一的json字符串
     */
    public static String getJsonString(int code, String msg, Map<String,Object> data){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);

        if(data != null){
            for(String key : data.keySet()){
                json.put(key,data.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJsonString(int code,String msg){
        return getJsonString(code,msg,null);
    }
}
