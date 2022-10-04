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
     * 根据用户id获得该用户被点赞的次数
     * @param userId 用户id
     * @return 该用户被点赞的次数
     */
    Integer selectLikeCountByUserId(Long userId);

    /**
     * 添加点赞记录
     */
    void addLikeRecord(LikeRecord likeRecord);
}
