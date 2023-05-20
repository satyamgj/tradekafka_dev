package com.finmamba.staticdatadal.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;



@Service
public class KafkaProducerService {
	
	@Autowired
	com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService producerSerivce;
	
	@Value("${app.producer-topic-name}")
	private String responseTopic;
	
	public boolean sendOnKafka(Object data, byte[] correlationId, Map<String, String> mapHeaders) {
		producerSerivce.send(
				MessageBuilder.withPayload(data).build()
				,responseTopic
				,correlationId
				,mapHeaders
				);
		return true;
	}
	
}
