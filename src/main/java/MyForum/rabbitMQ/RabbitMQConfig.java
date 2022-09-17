package MyForum.rabbitMQ;

import MyForum.common.JacksonObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Date: 2022/9/16
 * Time: 17:07
 *
 * @Author SillyBaka
 * Description： RabbitMQ的配置类
 **/
@Configuration
public class RabbitMQConfig {

    private final static ThreadPoolExecutor CONSUMER_THREAD_POOL;
    static {
        CONSUMER_THREAD_POOL = new ThreadPoolExecutor(16, 200, 0, TimeUnit.SECONDS,
                // 最多同时排队20000个任务
                new LinkedBlockingQueue<>(20000));
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, RabbitTemplateConfigurer configurer){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 设置消息转换器 以json为序列化器
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(new JacksonObjectMapper()));
        // 使用默认配置配置工厂
        configurer.configure(rabbitTemplate,connectionFactory);
        return rabbitTemplate;
    }
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory){

        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        // 给监听器（消费者）设置反序列化器为json
        simpleRabbitListenerContainerFactory.setMessageConverter(new Jackson2JsonMessageConverter(new JacksonObjectMapper()));
        // 使用自建线程池来代替默认的消费者线程池 提高线程复用性
        simpleRabbitListenerContainerFactory.setTaskExecutor(CONSUMER_THREAD_POOL);
        // 使用默认配置配置工厂
        configurer.configure(simpleRabbitListenerContainerFactory,connectionFactory);

        return simpleRabbitListenerContainerFactory;
    }

    // 业务交换机
    @Bean("myforum_exchange")
    public Exchange myforumExchange(){
        return ExchangeBuilder.topicExchange("myforum_exchange")
                .durable(true)
                .build();
    }
    // 通知用消息队列
    @Bean("notice_queue")
    public Queue noticeQueue(){
        return QueueBuilder.durable("notice_queue")
                .build();
    }
    @Bean
    public Binding bindingNotice(@Qualifier("myforum_exchange") Exchange myforumExchange,
                                 @Qualifier("notice_queue") Queue noticeQueue){
        return BindingBuilder.bind(noticeQueue)
                .to(myforumExchange)
                .with("#.notice.#")
                .noargs();
    }

    // 死信交换机
    @Bean("dead_exchange")
    public Exchange deadExchange(){
        return ExchangeBuilder.topicExchange("dead_exchange")
                .durable(true)
                .build();
    }

    // 延时队列 -- 计时用消息队列  --- 消息独立计时
    @Bean("timing_queue")
    public Queue timingQueue(){
        return QueueBuilder.durable("timing_queue")
                .deadLetterExchange("timing_dead_queue")
                .deadLetterRoutingKey("timing_dead.#")
                .build();
    }
    // 延时队列 -- 存消息用死信队列
    @Bean("timing_dead_queue")
    public Queue timingDeadQueue(){
        return QueueBuilder.durable("timing_dead_queue")
                .build();
    }

    @Bean
    public Binding bindingTiming(@Qualifier("myforum_exchange") Exchange myforumExchange,
                                 @Qualifier("timing_queue") Queue timingQueue){
        return BindingBuilder.bind(timingQueue)
                .to(myforumExchange)
                .with("timing.#")
                .noargs();
    }
    @Bean
    public Binding bindingDelayDeadQueue(@Qualifier("dead_exchange") Exchange deadExchange,
                                         @Qualifier("timing_dead_queue") Queue timingDeadQueue){
        return BindingBuilder.bind(timingDeadQueue)
                .to(deadExchange)
                .with("timing.#")
                .noargs();
    }

    // 检查用消息队列
    @Bean("check_queue")
    public Queue checkQueue(){
        return QueueBuilder.durable("check_queue")
                .build();
    }
    @Bean
    public Binding bindingCheck(@Qualifier("myforum_exchange") Exchange myforumExchange,
                                @Qualifier("check_queue") Queue checkQueue){
        return BindingBuilder.bind(checkQueue)
                .to(myforumExchange)
                .with("check.#")
                .noargs();
    }
}
