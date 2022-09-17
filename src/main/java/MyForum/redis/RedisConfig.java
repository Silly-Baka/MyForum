package MyForum.redis;

import MyForum.common.JacksonObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static MyForum.redis.RedisConstant.*;


/**
 * Date: 2022/8/16
 * Time: 16:32
 *
 * @Author SillyBaka
 * Description：
 **/
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        // String序列化器
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //todo  key采用String序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());

        //todo  value采用json序列化方式
//        // 定义一个json序列化器
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(new JacksonObjectMapper());

        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 定义一个json序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(new JacksonObjectMapper());
        // 模板配置
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                // 将缓存的key序列化器设置为string类型
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                // 将缓存的value序列化器设置为json类型
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer));
        // 对每一类信息的缓存配置
        Map<String,RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
        // 按需添加配置 （缓存的key前缀 + 过期时间 + 是否缓存空值等）
        // 例子如下:
        redisCacheConfigurationMap.put(LOGIN_CODE_KEY,configuration.prefixCacheNameWith(LOGIN_CODE_KEY)
                .entryTtl(Duration.ofSeconds(LOGIN_CODE_EXPIRED_TIME)));
        redisCacheConfigurationMap.put(LOGIN_TOKEN_KEY,configuration.prefixCacheNameWith(LOGIN_TOKEN_KEY)
                .entryTtl(Duration.ofSeconds(LOGIN_TOKEN_EXPIRED_TIME)));
        redisCacheConfigurationMap.put(FORGET_CODE_KEY,configuration.prefixCacheNameWith(FORGET_CODE_KEY)
                .entryTtl(Duration.ofSeconds(FORGET_CODE_EXPIRED_TIME)));
        redisCacheConfigurationMap.put(CACHE_POST_KEY,configuration.prefixCacheNameWith(CACHE_POST_KEY)
                .entryTtl(Duration.ofSeconds(CACHE_POST_EXPIRED_TIME)));
        redisCacheConfigurationMap.put(CACHE_POST_PAGE_KEY,configuration.prefixCacheNameWith(CACHE_POST_PAGE_KEY)
                .entryTtl(Duration.ofSeconds(CACHE_POST_EXPIRED_TIME)));
        redisCacheConfigurationMap.put(CACHE_COMMENT_PAGE_KEY,configuration.prefixCacheNameWith(CACHE_COMMENT_PAGE_KEY)
                .entryTtl(Duration.ofSeconds(CACHE_COMMENT_EXPIRED_TIME)));

        // 初始化redisCacheWriter
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);

        // 默认配置
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 将缓存的key序列化器设置为string类型
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                // 将缓存的value序列化器设置为json类型
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer))
                // 若没有设置过期时间 则默认是12小时
                .entryTtl(Duration.ofHours(12));

        return new RedisCacheManager(redisCacheWriter,defaultConfiguration,redisCacheConfigurationMap);
    }


}
