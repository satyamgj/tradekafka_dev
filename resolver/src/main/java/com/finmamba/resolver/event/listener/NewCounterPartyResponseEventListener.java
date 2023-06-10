package com.finmamba.resolver.event.listener;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.event.NewCounterpartyResponse;
import com.finmamba.resolver.operations.CounterPartyOperation;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewCounterPartyResponseEventListener implements ApplicationListener<NewCounterpartyResponse> {

	@Autowired
	EventTypeRepository eventTypeRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	KafkaProducerService kafkaProducerService;
	
	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;

	@Autowired
	Gson gson;

	@Autowired
	TransactionServiceDal dal;
	
	@Autowired
	CounterPartyOperation counterPartyOperation;

	@Override
	public void onApplicationEvent(NewCounterpartyResponse event) {

		log.info("Inside NewCounterpartyResponse");
		
		
		TransactionRequest request = event.getRequest();

		log.info( "Sending to counter party Id: " + request.getDestination());
		
		int responseType = event.getResponseType();
		
		log.info("responseType: " + responseType);
		
		if (responseType == 4) { // OUT_RESPONSE
			 counterPartyOperation.insertOutResponse(request); 
		} else if(responseType == 6) // NEGOTIATE
		{
			counterPartyOperation.insertOutNegotiation(request);
		}else if (responseType == 3) // REJECT 
		{
			counterPartyOperation.insertOutReject(request); 
		}
		
	}

}
