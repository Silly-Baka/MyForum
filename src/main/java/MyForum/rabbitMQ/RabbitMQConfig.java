package MyForum.rabbitMQ;

import MyForum.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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
@Slf4j
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

        // 设置confirm机制的回调函数
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                // 没有成功投递给交换机 重新投递
                if(!ack){
                    ReturnedMessage returnedMessage = correlationData.getReturned();
                    String exchange = returnedMessage.getExchange();

                    log.info("消息发送失败，目标交换机为：{} ",exchange);
                    log.info("消息将会被重新投递...");
                    Message message = returnedMessage.getMessage();
                    String routingKey = returnedMessage.getRoutingKey();

                    rabbitTemplate.convertAndSend(exchange,routingKey,message,correlationData);
                }
            }
        });
        // 设置return机制的回调函数
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                String exchange = returned.getExchange();
                String routingKey = returned.getRoutingKey();

                //todo 这里要判断是否是延迟队列中的消息 因为延迟队列中的消息进入死信队列也会调用该方法
                if("timing_dead_queue".equals(routingKey)){
                    return;
                }

                log.info("消息投递给队列失败，交换机为：{}，路由key为:{}",exchange,routingKey);

                CorrelationData correlationData = new CorrelationData();
                // 发送给业务用消息队列
                correlationData.setReturned(returned);
                rabbitTemplate.convertAndSend(exchange,routingKey,returned.getMessage(), correlationData);
            }
        });

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
                .with("notice.#")
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
                .deadLetterExchange("dead_exchange")
                .deadLetterRoutingKey("timing.#")
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

    // 生产者重发消息用消息队列
    @Bean("resend_queue")
    public Queue resendQueue(){
        return QueueBuilder.durable("resend_queue").build();
    }
    @Bean
    public Binding bindingResend(@Qualifier("myforum_exchange") Exchange myforumExchange,
                                 @Qualifier("resend_queue") Queue resendQueue){
        return BindingBuilder.bind(resendQueue)
                .to(myforumExchange)
                .with("resend.#")
                .noargs();
    }

    @Bean("elastic_post_add_queue")
    public Queue elasticAddQueue(){
        return QueueBuilder.durable("elastic_post_add_queue").build();
    }
    @Bean
    public Binding bindingElasticAdd(@Qualifier("myforum_exchange") Exchange myforumExchange,
                                     @Qualifier("elastic_post_add_queue") Queue elasticPostAddQueue){
        return BindingBuilder.bind(elasticPostAddQueue)
                .to(myforumExchange)
                .with("elastic_post_add.#")
                .noargs();
    }
    @Bean("elastic_post_update_queue")
    public Queue elasticUpdateQueue(){
        return QueueBuilder.durable("elastic_post_update_queue").build();
    }
    @Bean
    public Binding bindingElasticUpdate(@Qualifier("myforum_exchange") Exchange myforumExchange,
                                     @Qualifier("elastic_post_update_queue") Queue elasticPostUpdateQueue){
        return BindingBuilder.bind(elasticPostUpdateQueue)
                .to(myforumExchange)
                .with("elastic_post_update.#")
                .noargs();
    }
    @Bean("elastic_post_delete_queue")
    public Queue elasticDeleteQueue(){
        return QueueBuilder.durable("elastic_post_delete_queue").build();
    }
    @Bean
    public Binding bindingElasticDelete(@Qualifier("myforum_exchange") Exchange myforumExchange,
                                        @Qualifier("elastic_post_delete_queue") Queue elasticPostDeleteQueue){
        return BindingBuilder.bind(elasticPostDeleteQueue)
                .to(myforumExchange)
                .with("elastic_post_delete.#")
                .noargs();
    }
}
