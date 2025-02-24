package com.bt.yevhentucha.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@Profile("!test")
public class RabbitMQContainerConfig {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQContainerConfig.class);

    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3-management"))
            .withExposedPorts(5672);

    static {
        rabbitMQContainer.start();

        String rabbitHost = rabbitMQContainer.getHost();
        int rabbitPort = rabbitMQContainer.getAmqpPort();

        logger.info("RabbitMQ started at: amqp://{}:{}", rabbitHost, rabbitPort);

        System.setProperty("spring.rabbitmq.host", rabbitHost);
        System.setProperty("spring.rabbitmq.port", String.valueOf(rabbitPort));
        System.setProperty("spring.rabbitmq.username", rabbitMQContainer.getAdminUsername());
        System.setProperty("spring.rabbitmq.password", rabbitMQContainer.getAdminPassword());
    }

    @Bean
    public RabbitMQContainer rabbitMQContainer() {
        return rabbitMQContainer;
    }
}