package com.bt.yevhentucha.event;

import com.bt.yevhentucha.configuration.SensorConfig;
import com.bt.yevhentucha.domain.SensorData;
import com.bt.yevhentucha.exception.UnsupportedSensorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SensorDataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataConsumer.class);

    private final SensorConfig sensorConfig;

    public SensorDataConsumer(SensorConfig sensorConfig) {
        this.sensorConfig = sensorConfig;
    }

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void receiveMessage(String message) {
        logger.info("Received message: {}", message);

        SensorData sensorData = parseSensorData(message);
        if (sensorData != null && isThresholdExceeded(sensorData)) {
            logger.warn("ALARM! [{}] exceeded threshold: {}", sensorData.sensorId(), sensorData.value());
        }
    }

    private SensorData parseSensorData(String message) {
        try {
            String[] parts = message.split(";");
            String sensorId = parts[0].split("=")[1].trim();
            double value = Double.parseDouble(parts[1].split("=")[1].trim());
            return new SensorData(sensorId, value);
        } catch (Exception e) {
            logger.error("Error parsing sensor data: {}", message, e);
            return null;
        }
    }

    private boolean isThresholdExceeded(SensorData data) {
        double threshold = sensorConfig.thresholds().computeIfAbsent(data.sensorId(), k -> {
                    throw new UnsupportedSensorException("Unsupported configuration for sensor with id: " + k);
                }
        );
        return data.value() >= threshold;
    }
}