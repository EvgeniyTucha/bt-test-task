package com.bt.yevhentucha;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.bt.yevhentucha.configuration.SensorConfig;
import com.bt.yevhentucha.event.SensorDataConsumer;
import com.bt.yevhentucha.utils.InMemoryAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SensorDataConsumerTest {

    @MockitoBean
    private SensorConfig sensorConfig;

    @Autowired
    private SensorDataConsumer sensorDataConsumer;

    private InMemoryAppender appender;

    @Before
    public void setUp() {
        appender = new InMemoryAppender();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(SensorDataConsumer.class);
        logger.addAppender(appender);
    }

    @Test
    public void testReceiveMessage_ThresholdExceeded() {
        String message = "sensor_id=t1; value=36.5";

        when(sensorConfig.temperatureThreshold()).thenReturn(35.0);

        sensorDataConsumer.receiveMessage(message);

        List<String> logs = appender.getLog();
        assertTrue(logs.stream().anyMatch(log -> log.contains("ALARM! t1 exceeded threshold: 36.5")));
    }

    @Test
    public void testReceiveMessage_ThresholdNotExceeded() {
        String message = "sensor_id=t1; value=30.5";

        when(sensorConfig.temperatureThreshold()).thenReturn(35.0);

        sensorDataConsumer.receiveMessage(message);

        List<String> logs = appender.getLog();
        assertTrue(logs.stream().noneMatch(log -> log.contains("ALARM! t1 exceeded threshold: 30.5")));
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
