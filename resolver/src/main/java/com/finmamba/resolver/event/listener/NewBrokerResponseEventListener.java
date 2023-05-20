package com.finmamba.resolver.event.listener;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.entities.transaction.Daddy;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.event.NewBrokerResponse;
import com.finmamba.resolver.operations.InstitutionalEntityOperations;
import com.finmamba.resolver.operations.InstitutionalEntityResolver;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.finmamba.resolver.util.ApplicationConstants;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewBrokerResponseEventListener implements ApplicationListener<NewBrokerResponse> {

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	TransactionServiceDal dal;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	Gson gson;

	@Autowired
	InstitutionalEntityResolver entityResolver;

	@Override
	public void onApplicationEvent(NewBrokerResponse event) {

		log.info("Inside NewBrokerResponse");
		DaddyDto daddyDto = event.getDaddyDto();
		log.info(daddyDto.toString());

		// This parameter to be used to resolve JPA entity class
		// TODO: Ask satyam to replace source with details of the caller instance
		String realmName = daddyDto.getSource().getRealmName(); // ICICI
		String callerRealmName = event.getCallerRealmName(); // Kanji
		Daddy daddy = dal.findDaddyRequestByTransactionId(daddyDto.getTransactionId());
		String sourceRealmName = daddy.getSource().getRealmName();
		String sendToRealmName = event.getSendToRealmName();
		
		int responseType = event.getResponseType();

		// Kanji
		InstitutionalEntityOperations brokerOperationsHandler = entityResolver
				.getInstitutionalEntityOperationsHandle(callerRealmName);

		if (responseType == 1) { // FORWARD
			long eventCode = daddyDto.getEventType().getId();
			SuperInstitutionalEntityDto result;
			if (eventCode == ApplicationConstants.Event.IN_RESPONSE.getEventId()) {
				result = brokerOperationsHandler.insertOutResponse(daddyDto,sendToRealmName); //done
			} else if (eventCode == ApplicationConstants.Event.IN_NEGOTIATION.getEventId()) {
				result = brokerOperationsHandler.insertOutNegotiation(daddyDto,sendToRealmName); //done
			} else if (eventCode == ApplicationConstants.Event.IN_REJECT.getEventId()) {
				result = brokerOperationsHandler.insertOutReject(daddyDto, false,sendToRealmName); //done
			} else if(eventCode == ApplicationConstants.Event.IN_AWAITING.getEventId()) {
				log.info("Source " + realmName);
				log.info("Realms " + sendToRealmName,daddy.getSource().getRealmName());
				if(sendToRealmName.equalsIgnoreCase(realmName)) {
					log.info("Negotiating with " + sendToRealmName);
					result = brokerOperationsHandler.insertOutNegotiation(daddyDto,sendToRealmName); //done
				}
				else {
					log.info("Confirmation with " + sendToRealmName);
					result = brokerOperationsHandler.insertOutResponse(daddyDto,sendToRealmName); //done
				}
			}
		}else if(responseType == 2) { // BROKER REJECT
			brokerOperationsHandler.insertOutReject(daddyDto, true,sendToRealmName); //done
		}

	}
}
