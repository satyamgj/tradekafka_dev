package com.finmamba.resolver.event.listener;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.entities.transaction.SuperInstitutionalEntity;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.IciciWorldDto;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.event.NewCounterpartyResponse;
import com.finmamba.resolver.operations.InstitutionalEntityOperations;
import com.finmamba.resolver.operations.InstitutionalEntityResolver;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.finmamba.resolver.util.ApplicationConstants;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewCounterPartyResponseEventListener implements ApplicationListener<NewCounterpartyResponse> {

	@Autowired
	EventTypeRepository eventTypeRepository;
	
	@Autowired
	InstitutionalEntityResolver entityResolver;

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

	@Override
	public void onApplicationEvent(NewCounterpartyResponse event) {

		log.info("Inside NewCounterpartyResponse");
		
		
		DaddyDto daddyDto = event.getDaddyDto();
		log.info(daddyDto.toString());

		String sendToRealName = event.getSendToRealmName();
		log.info( "Sending to " + sendToRealName);
		
		int responseType = event.getResponseType();

		String callerRealmName = event.getCallerRealmName();
		
		String confirmedRealmName = "";

		log.info("responseType: " + responseType);

		// This parameter to be used to resolve JPA entity class
		String realmName = daddyDto.getSource().getRealmName();

		SuperInstitutionalEntity source = entityResolver.getInstitutionalEntityHandle(callerRealmName);
		InstitutionalEntityOperations sourceOperationsHandler = entityResolver
				.getInstitutionalEntityOperationsHandle(callerRealmName);
		
		SuperInstitutionalEntityDto result = null;
		if (responseType == 1) { // DEAL
			result = sourceOperationsHandler.insertOutResponse(daddyDto, sendToRealName); //kotak,icici
		} else if(responseType == 2) // NEGOTIATE
		{
			result = sourceOperationsHandler.insertOutNegotiation(daddyDto, sendToRealName); //kotak,icici
		}else if (responseType == 3) // REJECT 
		{
			result = sourceOperationsHandler.insertOutReject(daddyDto, false, sendToRealName); //kotak ,icic
		}
		if (result != null) {
			InstitutionalEntityOperations broker = entityResolver.getInstitutionalEntityOperationsHandle(sendToRealName);
			
			daddyDto.setSource(modelMapper.map(source.getSource(), InstitutionalEntityDto.class));

			SuperInstitutionalEntityDto result1 = null;
			if (responseType == 1) { // DEAL
				result1 = broker.insertInResponse(daddyDto);
			} else if(responseType == 2) { // NEGOTIATE
				result1 = broker.insertInNegotiation(result);
			}else if(responseType == 3) // REJECT
			{
				result1 = broker.insertInReject(result);
			}
			if (result1 != null) {
				
				if(result.getEventType().getId() == ApplicationConstants.Event.OUT_AWAITING.getEventId() 
						|| result.getEventType().getId() == ApplicationConstants.Event.OUT_NEGOTIATION.getEventId()
						|| result.getEventType().getId() == ApplicationConstants.Event.OUT_REJECT.getEventId()
						
						) {
					log.info("In Response saved");

					// TODO: Get from database
					String kafkaTopic = realmName + "." + "FXC" + "." + "MumbaiEntity" + "." + "spot-desk";

					log.info("Pushing on kafka topic: " + kafkaTopic);

					IciciWorldDto saveddto = modelMapper.map(result1, IciciWorldDto.class);
					List<IciciWorldDto> listSavedQuote = new ArrayList<>();
					listSavedQuote.add(saveddto);
					kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);
				}else {
					
					SuperInstitutionalEntityDto result2 = null;
					
					log.info("In Response saved");

					confirmedRealmName = institutionalEntityRepository.getById(daddyDto.getConfirmationInitiatedBy().getId()).getRealmName();

					InstitutionalEntityOperations confirmationInitiatedBy = entityResolver.getInstitutionalEntityOperationsHandle
							(
									confirmedRealmName
							);
					
					if(responseType == 1) {
						
						daddyDto.setSource(
								modelMapper.map(institutionalEntityRepository.findByRealmName(realmName), InstitutionalEntityDto.class)
								);
						
						 result2 = confirmationInitiatedBy.insertInResponse(daddyDto);
					}
					
					// TODO: Get from database
					String kafkaTopic = realmName + "." + "FXC" + "." + "MumbaiEntity" + "." + "spot-desk";

					String kafkaTopicToConfirmedRealm = confirmedRealmName + "." + "FXC" + "." + "MumbaiEntity" + "." + "spot-desk";

					
					log.info("Pushing on kafka topic: " + kafkaTopic);

					IciciWorldDto saveddto = modelMapper.map(result2, IciciWorldDto.class);
					List<IciciWorldDto> listSavedQuote = new ArrayList<>();
					listSavedQuote.add(saveddto);
					
					kafkaProducerService.send(listSavedQuote, kafkaTopic, "".getBytes() , null);
					kafkaProducerService.send(listSavedQuote, kafkaTopicToConfirmedRealm,  "".getBytes(), null);

				}
				
				
			}
		}
		else {
			log.info("Order already expired!");
			
			String kafkaTopic = callerRealmName + "." + "FXC" + "." + "MumbaiEntity" + "." + "spot-desk";
			
			SuperInstitutionalEntity response = new SuperInstitutionalEntity();
			
			response.setEventType(eventTypeRepository.getById(11L));
			
			SuperInstitutionalEntityDto responseDto= modelMapper.map(response, SuperInstitutionalEntityDto.class);
			
			kafkaProducerService.send(responseDto, kafkaTopic, "".getBytes() , null);

		}
	}

}
