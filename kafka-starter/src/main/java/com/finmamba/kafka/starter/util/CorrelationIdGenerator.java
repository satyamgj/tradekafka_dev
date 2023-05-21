package com.finmamba.kafka.starter.util;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class CorrelationIdGenerator {
	
	public String generateUniqueCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
