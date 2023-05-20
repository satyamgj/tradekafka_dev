package com.finmamba.resolver.event.listener;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.event.NewQuoteRequest;
import com.finmamba.resolver.operations.InstitutionalEntityOperations;
import com.finmamba.resolver.operations.InstitutionalEntityResolver;
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
	InstitutionalEntityResolver entityResolver;

	@Override
	public void onApplicationEvent(NewQuoteRequest event) {
		
		log.info("New Quote Request Event listener triggered");

		QuoteRequestDto quoteRequestDto = event.getQuoteRequestDto();

		String realmName = quoteRequestDto.getSource().getRealmName();

		InstitutionalEntityOperations sourceOperationsHandler = entityResolver
				.getInstitutionalEntityOperationsHandle(realmName);

		sourceOperationsHandler.dispatchRequestToBrokers(quoteRequestDto);

	}

}
