package com.finmamba.kafka.starter.autoconfigure.rest;

import com.finmamba.models.bus.SerialDataBusModel;

public interface KafkaRestOperations {
	
	public String send(final SerialDataBusModel message, final String topicName);

}
