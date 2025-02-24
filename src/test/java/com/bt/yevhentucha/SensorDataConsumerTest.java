package com.bt.yevhentucha;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.bt.yevhentucha.event.SensorDataConsumer;
import com.bt.yevhentucha.exception.UnsupportedSensorException;
import com.bt.yevhentucha.utils.InMemoryAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SensorDataConsumerTest {

    @Autowired
    private SensorDataConsumer sensorDataConsumer;

    private InMemoryAppender appender;

    @Before
    public void setUp() {
        assertNotNull(sensorDataConsumer, "SensorDataConsumer should not be null");
        appender = new InMemoryAppender();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(SensorDataConsumer.class);
        logger.addAppender(appender);
    }

    @Test
    public void testReceiveMessage_t1_ThresholdExceeded() {
        String message = "sensor_id=t1; value=36.5";

        sensorDataConsumer.receiveMessage(message);

        List<String> logs = appender.getLog();
        assertTrue(logs.stream().anyMatch(log -> log.contains("ALARM! [t1] exceeded threshold: 36.5")));
    }

    @Test
    public void testReceiveMessage_h1_ThresholdExceeded() {
        String message = "sensor_id=h1; value=52.3";

        sensorDataConsumer.receiveMessage(message);

        List<String> logs = appender.getLog();
        assertTrue(logs.stream().anyMatch(log -> log.contains("ALARM! [h1] exceeded threshold: 52.3")));
    }

    @Test
    public void testReceiveMessage_unsupportedSensor_ThresholdExceeded() {
        String message = "sensor_id=d1; value=100.3";

        assertThrows(UnsupportedSensorException.class, () -> sensorDataConsumer.receiveMessage(message));
    }

    @Test
    public void testReceiveMessage_ThresholdNotExceeded() {
        String message = "sensor_id=t1; value=30.5";
        sensorDataConsumer.receiveMessage(message);

        List<String> logs = appender.getLog();
        assertTrue(logs.stream().noneMatch(log -> log.contains("ALARM! [t1] exceeded threshold: 30.5")));
    }

    @Test
    public void testReceiveMessage_InvalidMessage() {
        String message = "invalid message";

        sensorDataConsumer.receiveMessage(message);

        List<String> logs = appender.getLog();
        assertTrue(logs.stream().anyMatch(log -> log.contains("Error parsing sensor data")));
    }

    @After
    public void tearDown() {
        // Clean up the appender after the test
        appender.clear();
    }
}
