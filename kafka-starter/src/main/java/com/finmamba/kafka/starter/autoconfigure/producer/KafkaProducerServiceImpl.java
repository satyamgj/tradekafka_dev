package com.finmamba.kafka.starter.autoconfigure.producer;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.finmamba.kafka.starter.util.CorrelationIdGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "KafkaProducer")
@Service
@ConditionalOnBean(value = KafkaProducerAutoConfiguration.class)
public class KafkaProducerServiceImpl implements KafkaProducerService {

	@Autowired
	KafkaTemplate<Integer, Message<Object>> kafkaTemplateMessageObject;

	@Autowired
	CorrelationIdGenerator correlationIdGenerator;

	private boolean success = false;

	@Override
	public boolean send(final Object message, final String topicName, byte[] correlationId,
			Map<String, String> mapKafkaHeaders) {

		Message<Object> kakfaMessage = MessageBuilder.withPayload(message).copyHeaders(mapKafkaHeaders).build();
		final ProducerRecord<Integer, Message<Object>> prodcuerRecord = new ProducerRecord<>(topicName, kakfaMessage);
		if (correlationId == null) {
			correlationId = correlationIdGenerator.generateUniqueCorrelationId().getBytes();
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
