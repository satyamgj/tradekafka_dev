package com.finmamba.resolver.event.listener;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.resolver.event.NewBrokerRequest;
import com.finmamba.resolver.event.NewConfirmRequest;
import com.finmamba.resolver.operations.CounterPartyOperation;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewConfirmRequestEventListener implements ApplicationListener<NewConfirmRequest>{
	
	@Autowired
	ModelMapper modelMapper;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	Gson gson;
	
	
	@Autowired
	CounterPartyOperation counterPartyOperation;
	
	public void onApplicationEvent(NewConfirmRequest event) {
		
		log.info("Inside NewConfirmRequest Event Listener");
		
		
		Boolean result = counterPartyOperation.confirmRequest(event.getConfirmRequest());
		
		
		log.info("NewBrokerRequest end with status: " + result );
		
	}

}
