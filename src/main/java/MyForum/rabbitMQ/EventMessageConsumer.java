package MyForum.rabbitMQ;

import MyForum.mapper.PostMapper;
import MyForum.mapper.elasticsearch.PostESMapper;
import MyForum.pojo.EventMessage;
import MyForum.pojo.Message;
import MyForum.pojo.Post;
import MyForum.service.MessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

import static MyForum.util.MyForumConstant.*;

/**
 * Date: 2022/9/16
 * Time: 20:12
 *
 * @Author SillyBaka
 * Description：异步接收事件消息，并且执行业务的消费者
 **/
@Component
@Slf4j
public class EventMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private MessageService messageService;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private PostESMapper postESMapper;
    @Resource
    private PostMapper postMapper;



    @RabbitListener(queues = {"notice_queue"})
    @RabbitHandler
    // 接收并处理关于通知的事件
    public void handlerNoticeEvent(org.springframework.amqp.core.Message message, EventMessage eventMessage, Channel channel){
        log.info("已接收到事件消息，事件id为:{}",eventMessage.getEventId());

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 消费消息
            Integer eventType = eventMessage.getEventType();
            Integer messageType = EVENT_TYPE_TO_MESSAGE_TYPE_MAP.get(eventType);

            // 封装成消息对象 准备放入数据库中
            // 不封装内容 由前端决定
            Message msg = new Message();
            msg.setMessageType(messageType);
            msg.setStatus(1);
            msg.setCreateTime(LocalDateTime.now());
            msg.setFromId(eventMessage.getOriginId());
            msg.setToId(eventMessage.getEntityId());
            if(eventType == EVENT_TYPE_LIKE || eventType == EVENT_TYPE_COMMENT){
                msg.setEntityId((long)eventMessage.getProperties().get("entityId"));
            }

            // 将通知放入数据库
            messageService.addMessage(msg);

            // 向原消息队列发送确认消息
            channel.basicAck(deliveryTag,false);

            // 发送消息给检查队列
            CorrelationData correlationData = new CorrelationData(String.valueOf(eventMessage.getEventId()));
//            correlationData.setReturned(new ReturnedMessage(null,666,"","myforum_exchange","check" ));

            rabbitTemplate.convertAndSend("myforum_exchange","check",eventMessage,correlationData);

        } catch (IOException e) {
            // 业务异常 发送拒绝消息 让消息队列重新发送消息
            try {
                channel.basicNack(deliveryTag,true,true);
            } catch (IOException ex) {
                log.error("服务器发生异常!");
            }
        }

        log.info("消息正在处理....");

    }

    // 处理添加帖子事件
    @RabbitListener(queues = "elastic_post_add_queue")
    @RabbitHandler
    public void handlerPostAddEvent(org.springframework.amqp.core.Message message, EventMessage eventMessage, Channel channel) throws IOException {

        log.info("已接收到添加帖子事件，事件id为:{}",eventMessage.getEventId());

        Long entityId = eventMessage.getEntityId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 从拓展消息表中取出帖子信息
            Map<String, Object> props = eventMessage.getProperties();
            Post post = (Post) props.get("post");

            // 将帖子信息存入es
            postESMapper.save(post);

            channel.basicAck(deliveryTag,false);

            CorrelationData correlationData = new CorrelationData(String.valueOf(eventMessage.getEventId()));
//            correlationData.setReturned(new ReturnedMessage(null,666,"","myforum_exchange","check" ));

            rabbitTemplate.convertAndSend("myforum_exchange","check",eventMessage,correlationData);

            log.info("添加帖子事件已处理完毕, 事件id为:{}",eventMessage.getEventId());

        } catch (Exception e) {
            channel.basicNack(deliveryTag,true,true);
            log.error("处理消息队列时发生异常...",e);
        }
    }

    // 处理修改帖子事件
    @RabbitListener(queues = "elastic_post_update_queue")
    @RabbitHandler
    public void handlerPostUpdateEvent(org.springframework.amqp.core.Message message, EventMessage eventMessage, Channel channel) throws IOException {

        log.info("已接收到修改帖子事件，事件id为:{}",eventMessage.getEventId());

        Long entityId = eventMessage.getEntityId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 直接从db中取出新的数据 同步进es
            Post post = postMapper.getPostById(entityId);

            postESMapper.save(post);

            channel.basicAck(deliveryTag,false);

            CorrelationData correlationData = new CorrelationData(String.valueOf(eventMessage.getEventId()));
//            correlationData.setReturned(new ReturnedMessage(null,666,"","myforum_exchange","check" ));

            rabbitTemplate.convertAndSend("myforum_exchange","check",eventMessage,correlationData);

            log.info("修改帖子事件已处理完毕, 事件id为:{}",eventMessage.getEventId());

        } catch (Exception e) {
            channel.basicNack(deliveryTag,true,true);
            log.error("处理消息队列时发生异常...",e);
        }
    }

    // 处理删除帖子事件
    @RabbitListener(queues = "elastic_post_delete_queue")
    @RabbitHandler
    public void handlerPostDeleteEvent(org.springframework.amqp.core.Message message, EventMessage eventMessage, Channel channel) throws IOException {

        log.info("已接收到删除帖子事件，事件id为:{}",eventMessage.getEventId());

        Long entityId = eventMessage.getEntityId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            postESMapper.deleteById(entityId);
            channel.basicAck(deliveryTag,false);

            CorrelationData correlationData = new CorrelationData(String.valueOf(eventMessage.getEventId()));
//            correlationData.setReturned(new ReturnedMessage(null,666,"","myforum_exchange","check" ));

            rabbitTemplate.convertAndSend("myforum_exchange","check",eventMessage,correlationData);

            log.info("删除帖子事件已处理完毕, 事件id为:{}",eventMessage.getEventId());
        } catch (Exception e) {
            channel.basicNack(deliveryTag,true,true);
            log.error("处理消息队列时发生异常...",e);
        }
    }

}

