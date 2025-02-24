package com.bt.yevhentucha.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "properties.sensor")
public record SensorConfig(int temperaturePort, int humidityPort, Map<String, Double> thresholds) {
}