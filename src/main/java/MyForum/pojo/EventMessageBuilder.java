package MyForum.pojo;

import java.util.Map;

/**
 * Date: 2022/9/16
 * Time: 19:55
 *
 * @Author SillyBaka
 * Description：消息事件的创建者  练习创建者模式
 **/
public class EventMessageBuilder {

    private EventMessage eventMessage = new EventMessage();

    public EventMessageBuilder eventId(Long eventId){
        eventMessage.setEventId(eventId);
        return this;
    }
    public EventMessageBuilder eventType(Integer eventType){
        eventMessage.setEventType(eventType);
        return this;
    }
    public EventMessageBuilder originId(Long originId){
        eventMessage.setOriginId(originId);
        return this;
    }
    public EventMessageBuilder targetId(Long targetId){
        eventMessage.setEntityId(targetId);
        return this;
    }
    public EventMessageBuilder properties(Map<String,Object> properties){
        eventMessage.setProperties(properties);
        return this;
    }
    public EventMessage build(){
        return eventMessage;
    }

}
