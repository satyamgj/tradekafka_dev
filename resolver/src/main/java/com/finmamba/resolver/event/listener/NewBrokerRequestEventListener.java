package com.finmamba.resolver.event.listener;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.resolver.event.NewBrokerRequest;
import com.finmamba.resolver.operations.InstitutionalEntityOperations;
import com.finmamba.resolver.operations.InstitutionalEntityResolver;
import com.finmamba.resolver.util.ApplicationConstants;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewBrokerRequestEventListener implements ApplicationListener<NewBrokerRequest> {

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	Gson gson;
	
	@Autowired
	InstitutionalEntityResolver entityResolver;

	@Override
	public void onApplicationEvent(NewBrokerRequest event) {

		log.info("Inside NewBrokerRequest for: " + event.getDaddyDto().getSource().getRealmName());
		
		DaddyDto daddyDto = event.getDaddyDto();
		log.info(daddyDto.toString());
		
		// This parameter to be used to resolve JPA entity class
		// TODO: Ask satyam to replace source with details of the caller instance
		String realmName = daddyDto.getSource().getRealmName();
		String callerRealmName = event.getCallerRealmName();
		
		InstitutionalEntityOperations sourceOperationsHandler = entityResolver.getInstitutionalEntityOperationsHandle(callerRealmName);
		
		// To decide whether it is quote request of a negotiation
		if(daddyDto.getEventType().getId() == ApplicationConstants.Event.IN_REQUEST.getEventId()) {
			Boolean result = sourceOperationsHandler.dispatchRequestToCounterparties(daddyDto, callerRealmName);
			log.info("NewBrokerRequest end with status: " + result );
		}
//		}else if(daddyDto.getEventType().getId() == ApplicationConstants.Event.IN_NEGOTIATION.getEventId()) {
//			sourceOperationsHandler.insertOutNegotiation(daddyDto);
//		}
		
		
		
		
		
	}

}
