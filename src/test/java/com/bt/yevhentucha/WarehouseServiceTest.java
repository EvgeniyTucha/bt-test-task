package com.bt.yevhentucha;

import com.bt.yevhentucha.event.SensorDataConsumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.awaitility.Awaitility.with;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
class WarehouseServiceTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoSpyBean
    private SensorDataConsumer sensorDataConsumer;

    private static final RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
                    .withExposedPorts(5672, 15672);

    @BeforeAll
    static void startContainer() {
        rabbitMQContainer.start();
    }

    @AfterAll
    static void stopContainer() {
        rabbitMQContainer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @Test
    void testSendAndReceiveMessage() {
        String testMessage = "sensor_id=t1; value=40";
        rabbitTemplate.convertAndSend("sensor.queue", testMessage);

        with().pollInterval(Duration.ofSeconds(1)).await().atMost(Duration.ofSeconds(10)).untilAsserted(
                () -> verify(sensorDataConsumer, atLeastOnce()).receiveMessage(any())
        );
    }
}
