package com.atguigu.gmall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack){
                log.error("消息没有到达交换机：" + cause);
            }
        });
        this.rabbitTemplate.setReturnCallback( (message, replyCode, replyText, exchange, routingKey) -> {
            log.error("消息发送失败。交换机：{}，路由键：{}，消息内容：{}", exchange, routingKey, new String(message.getBody()));
        });
    }


    /**
     * 定时关单的延时队列
     * @return
     */
    @Bean
    public Queue tlQueue() {
        return QueueBuilder.durable("ORDER_TTL_QUEUE")  // 延时队列名称
                .withArgument("x-message-ttl", 90000)   // 延时时间
                .withArgument("x-dead-letter-exchange", "ORDER_EXCHANGE")   // 死信交换机
                .withArgument("x-dead-letter-routing-key", "order.dead")    // 死信路由
                .build();
    }

    @Bean
    public Binding ttlBinding() {
        // return BindingBuilder.bind().to()
        return new Binding("ORDER_TTL_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "order.ttl", null);
    }

    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable("ORDER_DEAD_QUEUE").build();
    }

    @Bean
    public Binding deadBinding() {
        return new Binding("ORDER_DEAD_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "order.dead", null);
    }
}
