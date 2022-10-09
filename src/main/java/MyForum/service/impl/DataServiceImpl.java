package MyForum.service.impl;

import MyForum.redis.RedisConstant;
import MyForum.service.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static MyForum.redis.RedisConstant.COUNT_DAILY_ACTIVE_USER;

/**
 * Date: 2022/10/8
 * Time: 20:27
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
@Slf4j
public class DataServiceImpl implements DataService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public Long getDAU(LocalDate startDate, LocalDate endDate) {
        if(startDate.isAfter(endDate)){
            log.error("参数错误！无法获取日活跃记录");
            return 0L;
        }
        // 日期范围内的总日活跃用户
        long dauTotal = 0;
        // 遍历日期
        do{
            String dateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
            String key = COUNT_DAILY_ACTIVE_USER + dateStr;

            Long dau = (Long) redisTemplate.execute((RedisCallback<Object>) (connection) -> {
                return connection.bitCount(key.getBytes(StandardCharsets.UTF_8));
            });
            if(dau == null){
                log.error("redis服务端出现问题，尚未有该日期的日活跃记录");
            }else {
                dauTotal += dau;
            }

            // 日期+1天
            startDate = startDate.plus(1L, ChronoUnit.DAYS);
        }
        while (startDate.isBefore(endDate) || startDate.isEqual(endDate));

        return dauTotal;
    }

    @Override
    public Long getUV(LocalDate startDate, LocalDate endDate) {
        // 统计单日
        if(startDate.isEqual(endDate)){
            String dailyKey = RedisConstant.getCountUVDaily(startDate);

            return redisTemplate.opsForHyperLogLog().size(dailyKey);
        }

        String sectionKey = RedisConstant.getCountUVSection(startDate,endDate);
        // 统计日期区间
        List<String> keys = new ArrayList<>();
        while (startDate.isBefore(endDate) || startDate.isEqual(endDate)){
            keys.add(RedisConstant.getCountUVDaily(startDate));

            startDate = startDate.plus(1L,ChronoUnit.DAYS);
        }
        // 合并hyperloglog并返回合并结果
        Long uvTotal = redisTemplate.opsForHyperLogLog().union(sectionKey, keys.toArray(new String[keys.size()]));

        return uvTotal;
    }
}
