package MyForum.rabbitMQ;

import MyForum.pojo.EventMessage;
import MyForum.pojo.Message;
import MyForum.service.MessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static MyForum.util.MyForumConstant.EVENT_TYPE_TO_MESSAGE_TYPE_MAP;

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
    private final static ThreadPoolExecutor CONSUMER_THREAD_POOL;
    static {
        CONSUMER_THREAD_POOL = new ThreadPoolExecutor(16, 100, 0, TimeUnit.SECONDS,
                // 最多同时排队20000个任务
                new LinkedBlockingQueue<>(20000));
    }
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MessageService messageService;

    @RabbitListener(queues = {"notice_queue"})
    @RabbitHandler
    // 接收消息 将业务逻辑交给子线程处理
    public void handleEventMessage(org.springframework.amqp.core.Message message, EventMessage eventMessage, Channel channel){
        log.info("事件消息消费者已接收到事件消息，事件id为:{}",eventMessage.getEventId());

        // 终归还是一个线程来监听 + 处理  所以需要把任务传递给其他线程
        CONSUMER_THREAD_POOL.execute(new noticeTask(eventMessage,channel,message.getMessageProperties().getDeliveryTag()));

        log.info("已将消息传递给子线程处理.....");
    }

    /**
     * 处理消息，并异步发送通知的任务
     */
    private class noticeTask implements Runnable{

        private EventMessage eventMessage;
        private Channel channel;
        private long deliveryTag;

        public noticeTask(EventMessage eventMessage, Channel channel , long deliveryTag) {
            this.eventMessage = eventMessage;
            this.channel = channel;
            this.deliveryTag = deliveryTag;
        }
        @Override
        @Transactional
        public void run() {
            try {
                // 消费消息
                Integer eventType = eventMessage.getEventType();
                Integer messageType = EVENT_TYPE_TO_MESSAGE_TYPE_MAP.get(eventType);

                // 封装成消息对象 准备放入数据库中
                // 不封装内容 由前端决定
                Message message = new Message();
                message.setMessageType(messageType);
                message.setStatus(1);
                message.setCreateTime(LocalDateTime.now());
                message.setFromId(eventMessage.getOriginId());
                message.setToId(eventMessage.getTargetId());
                message.setEntityId((long)eventMessage.getProperties().get("entityId"));

                // 放入数据库
                messageService.addMessage(message);

                // 向原消息队列发送确认消息
                channel.basicAck(deliveryTag,false);

                //todo  并且将确认消息 发给 检查用消息队列   ---->  还需创建存放业务记录的表 和 检查用消息队列
                channel.basicPublish("myforum_exchange","check",null,null);

            } catch (IOException e) {
                // 业务异常 发送拒绝消息 让消息队列重新发送消息
                try {

                    channel.basicNack(deliveryTag,true,true);
                } catch (IOException ex) {
                    log.error("服务器发生异常!");
                }
            }
        }
    }
}
