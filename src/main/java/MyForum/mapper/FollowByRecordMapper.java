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
    void addFollowByRecord(FollowByRecord followByRecord);
}
