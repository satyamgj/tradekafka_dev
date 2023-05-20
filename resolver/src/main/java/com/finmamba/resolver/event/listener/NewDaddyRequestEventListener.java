package com.finmamba.resolver.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.entities.transaction.SuperInstitutionalEntity;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.event.NewDaddyRequest;
import com.finmamba.resolver.operations.InstitutionalEntityOperations;
import com.finmamba.resolver.operations.InstitutionalEntityResolver;
import com.finmamba.resolver.services.EventPublisher;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewDaddyRequestEventListener implements ApplicationListener<NewDaddyRequest> {

	@Autowired
	InstitutionalEntityResolver entityResolver;
	
	@Autowired
	EventPublisher eventPublisher;
	
	@Override
	public void onApplicationEvent(NewDaddyRequest event) {
		DaddyDto daddyDto = event.getDaddyDto();
		log.info(daddyDto.toString());
		
		// This parameter to be used to resolve JPA entity class
		String realmName = daddyDto.getSource().getRealmName();
		
		//SuperInstitutionalEntity source = entityResolver.getInstitutionalEntityHandle(realmName);
		
		InstitutionalEntityOperations sourceOperationsHandler = entityResolver.getInstitutionalEntityOperationsHandle(realmName);
		
		SuperInstitutionalEntityDto result = sourceOperationsHandler.insertDaddyRequest(daddyDto);
		
		if(result != null) {
			// Fire NewQuoteRequestEvent
			eventPublisher.publishNewQuoteRequestEvent(result);

			log.info("NewQuoteRequestEvent fired for transactionId: " + daddyDto.getTransactionId());
		}
		
//		eventPublisher.publishEvent(kotakWorldDto);
	}
}
