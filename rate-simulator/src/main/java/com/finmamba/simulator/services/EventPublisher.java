package com.finmamba.simulator.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.finmamba.simulator.event.StartRateSimulation;

@Component
public class EventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	public void startRateSimulation() {
		applicationEventPublisher.publishEvent(new StartRateSimulation(this));
	}
	
}
