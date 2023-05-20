package com.finmamba.entities.dblistener;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.concurrent.ListenableFuture;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendOnKafka {

	public boolean success = false;
	
	public boolean send(final Object message, final String topicName, byte[] correlationId,
			Map<String, String> mapKafkaHeaders,KafkaTemplate<Integer, Message<Object>> kafkaTemplateMessageObject) {

		
		
		Message<Object> kakfaMessage = MessageBuilder.withPayload(message).copyHeaders(mapKafkaHeaders).build();
		final ProducerRecord<Integer, Message<Object>> prodcuerRecord = new ProducerRecord<>(topicName, kakfaMessage);
		if (correlationId == null) {
			//correlationId = correlationIdGenerator.generateUniqueCorrelationId().getBytes();
			log.warn("Using generated correlation ID");
		}
		prodcuerRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId);

		log.info("Sending message on kafka topic: " + topicName);
		ListenableFuture<SendResult<Integer, Message<Object>>> future = kafkaTemplateMessageObject.send(prodcuerRecord);
		future.addCallback(new KafkaSendCallback<Integer, Message<Object>>() {

			@Override
			public void onSuccess(SendResult<Integer, Message<Object>> result) {
				log.debug("Message sending successful | Record: " + result.getProducerRecord().value().toString()
						+ " | Timestamp: " + result.getRecordMetadata().timestamp());
				success = true;
			}

			@Override
			public void onFailure(KafkaProducerException producerException) {
				log.error("Message sending failed | Error: " + producerException.getMessage());
				success = false;
			}

		});
		return success;
	}
	
}
