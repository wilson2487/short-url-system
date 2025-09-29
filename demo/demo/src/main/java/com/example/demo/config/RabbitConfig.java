package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "access_log_exchange";
    public static final String QUEUE_NAME = "access_log_queue";
    public static final String ROUTING_KEY = "access.log";

    /**
     * 創建直接交換器
     * 用於消息路由，根據路由鍵精確匹配消息
     * 
     * @return DirectExchange 直接交換器實例
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    /**
     * 創建消息隊列
     * 用於接收和存儲訪問日誌消息
     * 
     * @return Queue 消息隊列實例
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    /**
     * 綁定隊列到交換器
     * 將消息隊列與交換器連接，使用指定的路由鍵
     * 
     * @param queue 消息隊列
     * @param exchange 直接交換器
     * @return Binding 綁定關係
     */
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * 配置JSON消息轉換器
     * 用於將Java對象序列化為JSON格式進行消息傳輸
     * 
     * @return Jackson2JsonMessageConverter JSON轉換器實例
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitMQ模板
     * 用於發送消息到RabbitMQ，配置了JSON序列化器
     * 
     * @param connectionFactory RabbitMQ連接工廠
     * @param converter JSON消息轉換器
     * @return RabbitTemplate 配置好的RabbitMQ模板
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    /**
     * 配置RabbitMQ監聽器容器工廠
     * 用於創建消息監聽器容器，處理接收到的消息
     * 
     * @param connectionFactory RabbitMQ連接工廠
     * @param converter JSON消息轉換器
     * @return SimpleRabbitListenerContainerFactory 監聽器容器工廠
     */
    @Bean("rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}

