package MyForum.mapper;

import MyForum.pojo.FollowRecord;
import MyForum.pojo.FollowByRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * Date: 2022/9/26
 * Time: 20:42
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface FollowRecordMapper {

    /**
     * 根据用户的id查询该用户关注的人数
     * @param userId 用户id
     * @return 该用户关注的人数
     */
    Integer selectFollowCountByUserId(Long userId);

    void addFollowRecord(FollowRecord followRecord);
}
