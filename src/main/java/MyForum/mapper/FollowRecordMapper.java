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
    void addFollowRecord(FollowRecord followRecord);
}
