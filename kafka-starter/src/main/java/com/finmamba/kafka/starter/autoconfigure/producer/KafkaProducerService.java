package com.finmamba.kafka.starter.autoconfigure.producer;

import java.util.Map;

public interface KafkaProducerService {
	
	/**
	 * 
	 * @param message - Message to be published
	 * @param topicName - Message will be published on
	 * @param correlationId - Correlation id of the message
	 * @param mapKafkaHeaders - headers in the message
	 * @return
	 */
	public boolean send(Object message, String topicName, byte[] correlationId, Map<String, String> mapKafkaHeaders);
}	
