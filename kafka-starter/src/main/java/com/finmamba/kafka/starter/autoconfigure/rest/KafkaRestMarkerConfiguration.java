package com.finmamba.kafka.starter.autoconfigure.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import com.finmamba.kafka.starter.autoconfigure.consumer.KafkaConsumerAutoConfiguration;
import com.finmamba.kafka.starter.util.ApplicationProperties;

@Configuration
@ComponentScan(basePackages = "com.finmamba.kafka.starter.autoconfigure.rest", basePackageClasses = {
		ApplicationProperties.class, KafkaConsumerAutoConfiguration.class })
@EnableKafka
public class KafkaRestMarkerConfiguration {

	@Autowired
	KafkaRestMarker restMarker;
}
