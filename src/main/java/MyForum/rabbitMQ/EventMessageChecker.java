package MyForum.rabbitMQ;

import MyForum.pojo.EventMessage;
import cn.hutool.core.util.BooleanUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.IOException;

import static MyForum.redis.RedisConstant.EVENT_MESSAGE_CHECK_KEY;

/**
 * Date: 2022/9/17
 * Time: 16:15
 *
 * @Author SillyBaka
 * Description：用于检查事件消息的检查者（消费者）
 **/
@Component
@Slf4j
public class EventMessageChecker {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @RabbitListener(queues = {"timing_dead_queue","check_queue"})
    @RabbitHandler
    // 检查服务，监听延迟队列和确认消息队列的消息，并且检查库中是否含有该消息
    public void checkEventMessage(Message message, EventMessage eventMessage, Channel channel,
                                  // 通过Header注解 获取消息从哪个队列接收
                                  @Header(AmqpHeaders.CONSUMER_QUEUE) String queueName) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("检查服务已监听到来自队列 {} 的消息:{}",queueName,eventMessage);

        String redisKey = EVENT_MESSAGE_CHECK_KEY;
        try {
            // 如果是来自确认消息队列 则直接入库
            if("check_queue".equals(queueName)){

                redisTemplate.opsForHash().put(redisKey,String.valueOf(eventMessage.getEventId()),eventMessage);
                log.info("该消息成功接收，已入库记录");

            // 如果来自延迟队列
            }else if("timing_dead_queue".equals(queueName)){
            // 如果来自延迟队列 则先检查库中有无该消息
                Boolean hasMessage = redisTemplate.opsForHash().hasKey(redisKey,String.valueOf(eventMessage.getEventId()));
                // 若库中有该消息 则丢弃消息（无操作）
                if(BooleanUtil.isTrue(hasMessage)){
                    log.info("该消息已成功接收并入库，丢弃本消息");
                }else {
                // 若无，则发送消息给生产者的重发队列 --- 让生产者接收，并重新发送
                    CorrelationData correlationData = new CorrelationData(String.valueOf(eventMessage.getEventId()));

                    rabbitTemplate.convertAndSend("myforum_exchange","resend", eventMessage, correlationData);
                    log.info("消息已丢失，已发送重发消息给生产者");
                }
            }
            // 确认消息已消费
            channel.basicAck(deliveryTag,false);

        } catch (IOException e) {
            log.error("检查服务出现异常，消息请求回到队列...");
            channel.basicNack(deliveryTag,true,true);
        }
    }
}
