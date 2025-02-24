package com.bt.yevhentucha.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue sensorQueue() {
        return new Queue("sensor.queue", true); // true makes it durable
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("sensorExchange");
    }

    @Bean
    public Binding binding(Queue sensorQueue, TopicExchange exchange) {
        return BindingBuilder.bind(sensorQueue)
                .to(exchange)
                .with("sensor.#"); // routing key
    }
}
