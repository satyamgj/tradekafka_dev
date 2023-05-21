package com.finmamba.kafka.starter.autoconfigure.producer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.Message;

import com.finmamba.kafka.starter.util.ApplicationProperties;

@Configuration
@ConditionalOnBean(value = KafkaProducerMarker.class)
public class KafkaProducerAutoConfiguration {
	
	@Autowired
	ApplicationProperties applicationProperties;
	
	@Autowired
	KafkaProperties kafkaProperties;
	
	@Bean
	public ProducerFactory<Integer, String> producerFactoryString() {
		return new DefaultKafkaProducerFactory<>(senderProps());
	}

	@Bean
	public ProducerFactory<Integer, Object> producerFactoryObject() {
		return new DefaultKafkaProducerFactory<>(senderProps());
	}
	
	@Bean
	public ProducerFactory<Integer, Message<Object>> producerFactoryMessageObject() {
		return new DefaultKafkaProducerFactory<>(senderProps());
	}

	private Map<String, Object> senderProps() {
		Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
		return props;
	}

	@Bean
	public KafkaTemplate<Integer, String> kafkaTemplateString(ProducerFactory<Integer, String> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	public KafkaTemplate<Integer, Object> kafkaTemplateObject(ProducerFactory<Integer, Object> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
	
	@Bean
	public KafkaTemplate<Integer, Message<Object>> kafkaTemplateMessageObject(ProducerFactory<Integer, Message<Object>> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
	
    
	
}
