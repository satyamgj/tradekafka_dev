package com.finmamba.kafka.starter;

import lombok.Value;

@Value(staticConstructor = "of")
public class KafkaEventModel {
	String message;
	String topic;
	byte[] correlationId;
}
