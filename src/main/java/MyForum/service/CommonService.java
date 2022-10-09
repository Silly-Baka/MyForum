package MyForum.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Date: 2022/9/5
 * Time: 21:58
 *
 * @Author SillyBaka
 * Description：点赞服务接口
 **/
public interface CommonService {
    /**
     * 根据类型和id点赞对应实体
     * @param entityType 实体类型
     * @param entityId 实体id
     * @param entityUserId 实体发出者的id
     * @return 点赞结果
     */
    Map<String,Object> like(Integer entityType, Long entityId, Long entityUserId);

    /**
     * 关注目标用户
     * @param userId 目标用户id
     * @return 点赞结果
     */
    Map<String,Object> follow(Long userId);
}
