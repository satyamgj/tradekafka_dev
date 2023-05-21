package com.finmamba.kafka.starter.autoconfigure.producer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.finmamba.kafka.starter.util.ApplicationProperties;

@ComponentScan(basePackages = "com.finmamba.kafka.starter.autoconfigure.producer", basePackageClasses = ApplicationProperties.class)
@EnableConfigurationProperties
public class KafkaProducerMarkerConfiguration {
}
