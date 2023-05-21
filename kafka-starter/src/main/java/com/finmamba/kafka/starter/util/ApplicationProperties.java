package com.finmamba.kafka.starter.util;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class ApplicationProperties {
	@NotBlank
	private String producerTopicName;
	
	@NotBlank
	private String consumerTopicName;
}
