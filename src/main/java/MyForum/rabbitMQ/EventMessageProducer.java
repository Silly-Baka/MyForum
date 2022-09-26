package MyForum.rabbitMQ;

import MyForum.pojo.EventMessage;
import MyForum.pojo.EventMessageBuilder;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.Map;

import static MyForum.redis.RedisConstant.EVENT_MESSAGE_ONLY_ID_KEY;
import static MyForum.redis.RedisConstant.EVENT_MESSAGE_ORIGIN_KEY;

/**
 * Date: 2022/9/16
 * Time: 20:04
 *
 * @Author SillyBaka
 * Description：消息事件的生产者 -- 用于发消息给消息队列
 **/
@Component
@Slf4j
public class EventMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 发送消息给消息队列
     */
    @Transactional
    public void sendMessage(EventMessage eventMessage){
        if(eventMessage == null){
            log.error("传入的事件消息对象为空，发送失败！");
            return;
        }
        // 先将业务消息入库（用Redis的Hash存）
        String key = EVENT_MESSAGE_ORIGIN_KEY;
        // hashKey是业务消息的唯一id
        redisTemplate.opsForHash().put(key, String.valueOf(eventMessage.getEventId()),eventMessage);

        CorrelationData correlationData = new CorrelationData(String.valueOf(eventMessage.getEventId()));
        // 发送给业务用消息队列
        correlationData.setReturned(new ReturnedMessage(null,666,"","myforum_exchange","notice"));
        rabbitTemplate.convertAndSend("myforum_exchange","notice",eventMessage,correlationData);

        // 发送给延时队列 --- 消息补偿策略 用于保障消息可靠性
        correlationData.setReturned(new ReturnedMessage(null,666,"","myforum_exchange","timing"));
        rabbitTemplate.convertAndSend("myforum_exchange", "timing", eventMessage,
                new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        // 设置延时队列延时时间 5分钟
                        message.getMessageProperties().setExpiration("300000");
                        return message;
                    }
                },correlationData);
    }

    /**
     * 创建一个事件消息
     * @param eventType 事件类型
     * @param originId 事件触发者id
     * @param targetId 事件目标id
     * @param prop 其他参数
     * @return 事件对象
     */
    public EventMessage createEventMessage(Integer eventType, Long originId, Long targetId, Map<String,Object> prop){
        // 用Redis获得唯一id
        Long onlyId = redisTemplate.opsForValue().increment(EVENT_MESSAGE_ONLY_ID_KEY);

        EventMessage eventMessage = new EventMessageBuilder()
                .eventId(onlyId)
                .eventType(eventType)
                .originId(originId)
                .targetId(targetId)
                .properties(prop)
                .build();

        return eventMessage;
    }

    // 监听重发队列，若有消息 则需要将其重新发送给消费者
    @RabbitListener(queues = "resend_queue")
    @RabbitHandler
    public void resendMessage(Message message,EventMessage eventMessage, Channel channel) throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            sendMessage(eventMessage);
            channel.basicAck(deliveryTag,true);
            log.info("重发消息成功，消息为：{}",eventMessage);
        } catch (IOException e) {
            log.error("重发消息服务异常,消息为：{}",eventMessage);
            channel.basicNack(deliveryTag,true,true);
        }

    }
}
