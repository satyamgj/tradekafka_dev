package com.finmamba.resolver.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.KanjiWorldDto;
import com.finmamba.models.dto.KotakWorldDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.resolver.event.NewBrokerRequest;
import com.finmamba.resolver.event.NewBrokerResponse;
import com.finmamba.resolver.event.NewCounterpartyResponse;
import com.finmamba.resolver.event.NewDaddyRequest;
import com.finmamba.resolver.event.NewNegotiationBrokerResponse;
import com.finmamba.resolver.event.NewNegotiationResponse;
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
	
	
	public void publishNewQuoteRequestEvent(final SuperInstitutionalEntityDto entityDto) {
		applicationEventPublisher.publishEvent(new NewQuoteRequest(this, entityDto));
	}
	
	public void publishNewBrokerRequestEvent(final DaddyDto daddyDto, final String callerRealmName) {
		applicationEventPublisher.publishEvent(new NewBrokerRequest(this, daddyDto, callerRealmName));
	}
	
	public void publishNewCounterpartyResponseEvent(final DaddyDto daddyDto, final int responseType, final String callerRealmName, String sendToRealmName) {
		applicationEventPublisher.publishEvent(new NewCounterpartyResponse(this, daddyDto, responseType, callerRealmName,sendToRealmName));
	}
	
	public void publishNewBrokerResponseEvent(final DaddyDto daddyDto, final String callerRealmName) {
		applicationEventPublisher.publishEvent(new NewBrokerResponse(this, daddyDto, callerRealmName));
	}
	
	public void publishNewBrokerResponseEvent(final DaddyDto daddyDto, final String callerRealmName, int responseType, String sendToRealmName) {
		applicationEventPublisher.publishEvent(new NewBrokerResponse(this, daddyDto, callerRealmName, responseType,sendToRealmName));
	}
	
	
	

	public void publishEvent(final KotakWorldDto kotakWorldDto) {
		applicationEventPublisher.publishEvent(new NewQuoteRequest(this, kotakWorldDto));
	}
	
//	public void publishEvent(final KanjiWorldDto kanjiWorldDto) {
//		applicationEventPublisher.publishEvent(new NewBrokerRequest(this, kanjiWorldDto));
//	}
	
//	public void publishEvent(final IciciWorldDto iciciWorldDto, int responseType) {
//		applicationEventPublisher.publishEvent(new NewCounterpartyResponse(this, iciciWorldDto, responseType));
//	}
	
//	public void publishEvent(final KanjiWorldDto kanjiWorldDto, boolean b) {
//		applicationEventPublisher.publishEvent(new NewBrokerResponse(this, kanjiWorldDto));
//	}
	
	public void publishEvent(final KanjiWorldDto kanjiWorldDto, int responseType) {
		applicationEventPublisher.publishEvent(new NewNegotiationBrokerResponse(this, kanjiWorldDto, responseType));
	}
	
	// Negotiation response
	public void publishEvent(final KotakWorldDto kotakWorldDto, int responseType) {
		applicationEventPublisher.publishEvent(new NewNegotiationResponse(this, kotakWorldDto, responseType));
	}
}
