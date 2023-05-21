package com.finmamba.kafka.starter.autoconfigure.consumer;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.finmamba.kafka.starter.KafkaEventModel;
import com.finmamba.kafka.starter.util.ApplicationProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "KafkaConsumer")
@Service
@ConditionalOnBean(value = KafkaConsumerAutoConfiguration.class)
public class KafkaConsumerEventPublisherAdapter {
	private Consumer<Object> publishEventConsumer;

	@Autowired
	ApplicationProperties applicationProperties;

	public KafkaConsumerEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
		this.publishEventConsumer = applicationEventPublisher::publishEvent;
	}

	@KafkaListener(id = "consumer1", topics = "${app.consumer-topic-name}")
	public void listen(@Payload String in, @Header(name = KafkaHeaders.CORRELATION_ID) byte[] correlationID) {
		log.debug("Publishing Received message on topic: " + applicationProperties.getConsumerTopicName()
				+ " with CorrelationID: " + new String(correlationID, StandardCharsets.UTF_8));
		publishEvent(KafkaEventModel.of(in, applicationProperties.getConsumerTopicName(), correlationID));
	}

	private <T> void publishEvent(T event) {
		try {
			publishEventConsumer.accept(event);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}
}
