package MyForum.pojo;

import MyForum.DTO.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Date: 2022/8/8
 * Time: 12:04
 *
 * @Author SillyBaka
 *
 * Description：帖子详细信息
 **/
@Data
@NoArgsConstructor
@Document(indexName = "post")
public class Post implements Serializable {
    /**
     * 帖子id
     */
    @Id
    private Long id;
    /**
     * 发帖子的用户id
     */
    @Field(type = FieldType.Long)
    private Long userId;
    /**
     * 帖子标题
     */
    @Field(type = FieldType.Text)
    private String title;
    /**
     * 帖子内容
     */
    @Field(type = FieldType.Text)
    private String content;
    /**
     * 帖子类型 0为普通贴 1为置顶贴
     */
    @Field(type = FieldType.Keyword)
    private Integer type;
    /**
     * 帖子状态 0-正常  1-精品  2-撤销、拉黑
     */
    @Field(type = FieldType.Keyword)
    private Integer status;
    /**
     * 帖子创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
    /**
     * 帖子的热度
     */
    @Field(type = FieldType.Double)
    private Double score;
    /**
     * 回帖数量
     */
    @Field(type = FieldType.Keyword)
    private Integer commentCount;

    /**
     * 用户信息
     */
    private UserDTO user;
    /**
     * 当前用户已经点赞
     */
    private Boolean isLiked;
    /**
     * 点赞数
     */
    private Long likeCount;
}
