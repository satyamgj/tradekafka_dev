package com.finmamba.resolver.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.event.NewBrokerRequest;
import com.finmamba.resolver.event.NewBrokerResponse;
import com.finmamba.resolver.event.NewCounterpartyResponse;
import com.finmamba.resolver.event.NewQuoteRequest;

@Component
public class EventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	public void publishEvent(final QuoteRequestDto quoteRequestDto) {
		applicationEventPublisher.publishEvent(new NewQuoteRequest(this, quoteRequestDto));
	}
	
	
	public void publishNewBrokerRequestEvent(TransactionRequest requestBrokerDispatchRequest) {
		applicationEventPublisher.publishEvent(new NewBrokerRequest(this, requestBrokerDispatchRequest));
	}
	
	public void publishNewCounterpartyResponseEvent(final TransactionRequest request, int responseType) {
		applicationEventPublisher.publishEvent(new NewCounterpartyResponse(this, request, responseType));
	}
	
	public void publishNewBrokerResponseEvent(final DaddyDto daddyDto, final String callerRealmName) {
		applicationEventPublisher.publishEvent(new NewBrokerResponse(this, daddyDto, callerRealmName));
	}
	
	public void publishNewBrokerResponseEvent(final DaddyDto daddyDto, final String callerRealmName, int responseType, String sendToRealmName) {
		applicationEventPublisher.publishEvent(new NewBrokerResponse(this, daddyDto, callerRealmName, responseType,sendToRealmName));
	}
	
}
