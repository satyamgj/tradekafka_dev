package com.finmamba.kafka.starter.autoconfigure.consumer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.finmamba.kafka.starter.util.ApplicationProperties;

@ComponentScan(basePackages = "com.finmamba.kafka.starter.autoconfigure.consumer", basePackageClasses = ApplicationProperties.class)
@EnableConfigurationProperties
public class KafkaConsumerMarkerConfiguration {

}
