package com.finmamba.simulator.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.finmamba.simulator.services.EventPublisher;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SpringApplicationsStartupListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	EventPublisher eventPublisher;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("Starting rate stream");
		eventPublisher.startRateSimulation();
	}

}
