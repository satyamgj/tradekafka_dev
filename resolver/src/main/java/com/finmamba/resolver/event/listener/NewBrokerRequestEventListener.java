package com.finmamba.resolver.event.listener;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.resolver.event.NewBrokerRequest;
import com.finmamba.resolver.operations.CounterPartyOperation;
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
	CounterPartyOperation counterPartyOperation;

	@Override
	public void onApplicationEvent(NewBrokerRequest event) {
		

		log.info("Inside NewBrokerRequest for: " + event.getBrokerDispatchRequest().getSource());
		
		
		Boolean result = counterPartyOperation.dispatchRequestToCounterparties(event.getBrokerDispatchRequest()
				, event.getBrokerDispatchRequest().getRequestInitiatedBy() );
		
		
		log.info("NewBrokerRequest end with status: " + result );
		
	}

}
