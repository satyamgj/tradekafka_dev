package com.finmamba.kafka.starter.autoconfigure.rest;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.finmamba.kafka.starter.util.ApplicationProperties;
import com.finmamba.models.bus.SerialDataBusModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "KafkaRestService")
@ConditionalOnBean(value = KafkaRestAutoConfiguration.class)
public class KafkaRestService implements KafkaRestOperations {
	ReplyingKafkaTemplate<String, Message<SerialDataBusModel>, Object> replyingKafkaTemplate;
	
	@Autowired
	ApplicationProperties applicationProperties;
	
	public KafkaRestService(ReplyingKafkaTemplate<String, Message<SerialDataBusModel>, Object> replyingKafkaTemplate) {
		this.replyingKafkaTemplate = replyingKafkaTemplate;
	}

	@Override
	public String send(SerialDataBusModel message, String topicName) {
		try {
			log.debug("Payload sent-> " + message.toString());
			Message<SerialDataBusModel> kakfaMessage = MessageBuilder.withPayload(message).build();
			
			// create producer record
			ProducerRecord<String, Message<SerialDataBusModel>> producerRecord = new ProducerRecord<>(topicName, kakfaMessage);

			// set reply topic in header
			producerRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC,
					applicationProperties.getConsumerTopicName().getBytes()));

			// post in kafka topic
			RequestReplyFuture<String, Message<SerialDataBusModel>, Object> sendAndReceive = replyingKafkaTemplate
					.sendAndReceive(producerRecord, Duration.ofMillis(60000));

			// confirm if producer produced successfully
			SendResult<String, Message<SerialDataBusModel>> sendResult = sendAndReceive.getSendFuture().get();

			// print all headers
			sendResult.getProducerRecord().headers()
					.forEach(header -> log.debug(header.key() + ":" + header.value().toString()));

			// get consumer record
			ConsumerRecord<String, Object> consumerRecord = sendAndReceive.get();

			// return consumer value
			log.debug(consumerRecord.value().toString());
			log.info("Publishing consumer record on topic: " + applicationProperties.getConsumerTopicName());
			return consumerRecord.value().toString();
		} catch (Exception ex) {
			log.error("Error in replyingKafkaTemplateSend: " + ex.getMessage());
		}
		return null;
	}

}
