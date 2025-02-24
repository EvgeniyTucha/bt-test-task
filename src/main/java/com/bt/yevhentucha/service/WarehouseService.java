package com.bt.yevhentucha.service;

import com.bt.yevhentucha.configuration.SensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableConfigurationProperties(SensorConfig.class)
@SpringBootApplication
@ComponentScan(basePackages = {"com.bt.yevhentucha.configuration", "com.bt.yevhentucha.event"})
public class WarehouseService implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(WarehouseService.class);

    private final RabbitTemplate rabbitTemplate;
    private final SensorConfig sensorConfig;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Value("${rabbitmq.queue}")
    private String queueName;

    public WarehouseService(RabbitTemplate rabbitTemplate, SensorConfig sensorConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.sensorConfig = sensorConfig;
    }

    @Override
    public void run(String... args) {
        executorService.submit(() -> listenForSensors(sensorConfig.temperaturePort()));
        executorService.submit(() -> listenForSensors(sensorConfig.humidityPort()));

        // Shutdown hook to clean up threads on exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void listenForSensors(int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            logger.info("Listening for UDP packets on port: {}", port);
            byte[] buffer = new byte[1024];

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                logger.info("received message via socket: {}", message);

                sendToQueue(message);
            }
        } catch (Exception e) {
            if (!Thread.currentThread().isInterrupted()) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void sendToQueue(String message) {
        rabbitTemplate.convertAndSend(queueName, message);
        logger.info("Sent to RabbitMQ: {}", message);
    }

    private void shutdown() {
        logger.info("Shutting down Warehouse Service...");
        executorService.shutdownNow();
    }
}
