package MyForum.mapper;

import MyForum.pojo.FollowByRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * Date: 2022/9/26
 * Time: 20:44
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface FollowByRecordMapper {
    /**
     * 根据用户id 获得该用户被多少人关注
     * @param userId 用户id
     * @return 被关注的数量
     */
    Integer selectFollowedCountByUserId(Long userId);

    Integer selectOneByUserIdAndFollowedId(Long userId, Long followedId);

    void addFollowByRecord(FollowByRecord followByRecord);
}
