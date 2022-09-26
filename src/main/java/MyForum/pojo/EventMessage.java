package MyForum.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Date: 2022/9/16
 * Time: 19:43
 *
 * @Author SillyBaka
 * Description：事件消息，用于记录点赞、关注、评论等事件
 * // 此类用建造者模式创建 练习
 **/
@Data
@NoArgsConstructor
public class EventMessage {
    /**
     * 事件的唯一id
     */
    private Long eventId;
    /**
     * 事件类型
     */
    private Integer eventType;
    /**
     * 事件触发者id
     */
    private Long originId;
    /**
     * 事件目标id
     */
    private Long targetId;

    /**
     * 用于拓展的参数表
     */
    private Map<String,Object> properties;


    public Long getEventId() {
        return eventId;
    }

    public Integer getEventType() {
        return eventType;
    }

    public Long getOriginId() {
        return originId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
