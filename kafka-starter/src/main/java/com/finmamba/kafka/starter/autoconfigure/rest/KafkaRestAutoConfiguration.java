package com.finmamba.kafka.starter.autoconfigure.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.messaging.Message;

import com.finmamba.kafka.starter.util.ApplicationProperties;
import com.finmamba.models.bus.SerialDataBusModel;

@Configuration
@ConditionalOnBean(value = KafkaRestMarker.class)
public class KafkaRestAutoConfiguration {

	@Autowired
	KafkaProperties kafkaProperties;

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Autowired
	ApplicationProperties applicationProperties;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroup;

	@Bean
	public ReplyingKafkaTemplate<String, Message<SerialDataBusModel>, Object> replyingKafkaTemplate(
			ProducerFactory<String, Message<SerialDataBusModel>> pf,
			ConcurrentKafkaListenerContainerFactory<String, Object> factory) {

		ConcurrentMessageListenerContainer<String, Object> replyContainer = factory
				.createContainer(applicationProperties.getConsumerTopicName());
		replyContainer.getContainerProperties().setMissingTopicsFatal(false);
		replyContainer.getContainerProperties().setGroupId(consumerGroup);
		replyContainer.getContainerProperties().getKafkaConsumerProperties()
				.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		replyContainer.getContainerProperties().getKafkaConsumerProperties()
				.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		replyContainer.getContainerProperties().getKafkaConsumerProperties()
				.putIfAbsent(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		replyContainer.getContainerProperties().getKafkaConsumerProperties()
				.putIfAbsent(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class.getName());

		return new ReplyingKafkaTemplate<>(pf, replyContainer);
	}

	/*@Bean
	public KafkaTemplate<String, String> replyTemplate(ProducerFactory<String, String> pf,
			ConcurrentKafkaListenerContainerFactory<String, Object> factory) {
		KafkaTemplate<String, Object> kafkaTemplate = null;
		kafkaTemplate = new KafkaTemplate<>(pf);
		factory.getContainerProperties().setMissingTopicsFatal(false);
		factory.setReplyTemplate(kafkaTemplate);
		return kafkaTemplate;
	}*/

	@Bean
	public ProducerFactory<String, Message<SerialDataBusModel>> producerFactoryMessageCustom() {
		return new DefaultKafkaProducerFactory<>(senderProps());
	}

	@Bean
	public ProducerFactory<String, String> producerFactoryMessageString() {
		return new DefaultKafkaProducerFactory<>(senderProps());
	}

	private Map<String, Object> senderProps() {
		Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
		return props;
	}
	
	@Bean
	public KafkaRestAutoConfiguration KafkaRestAutoConfigurationBean() {
		return this;
	}
	
}
