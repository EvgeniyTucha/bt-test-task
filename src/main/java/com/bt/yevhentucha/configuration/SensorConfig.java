package com.bt.yevhentucha.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "properties.sensor")
public record SensorConfig(int temperaturePort, int humidityPort, double temperatureThreshold, double humidityThreshold) {
}