package MyForum.mapper;

import MyForum.pojo.LikeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * Date: 2022/9/26
 * Time: 16:04
 *
 * @Author SillyBaka
 * Description：
 **/
@Mapper
public interface LikeRecordMapper {
    /**
     * 添加点赞记录
     */
    void addLikeRecord(LikeRecord likeRecord);
}
