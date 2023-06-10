package com.finmamba.resolver.event.listener;

import java.time.Instant;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.resolver.event.NewQuoteRequest;
import com.finmamba.resolver.operations.CounterPartyOperation;
import com.finmamba.resolver.services.EventPublisher;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewQuoteRequestEventListener implements ApplicationListener<NewQuoteRequest> {

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	EventPublisher eventPublisher;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	Gson gson;

	
	@Autowired
	CounterPartyOperation counterPartyOperation;

	@Override
	public void onApplicationEvent(NewQuoteRequest event) {
		
		log.info("NewQuoteRequestEventListener  | START " + Instant.now());

		
		log.info("New Quote Request Event listener triggered");

		QuoteRequestDto quoteRequestDto = event.getQuoteRequestDto();

		String realmName = quoteRequestDto.getSource().getRealmName();
		
		counterPartyOperation.dispatchRequestToBrokers(quoteRequestDto);
		
		log.info("Dispatched Quote Request to brokers of " + realmName);
		
		log.info("NewQuoteRequestEventListener  | END " + Instant.now());


	}

}
