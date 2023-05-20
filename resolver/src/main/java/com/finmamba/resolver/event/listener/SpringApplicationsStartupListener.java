package com.finmamba.resolver.event.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.finmamba.resolver.operations.EntityResolverMap;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SpringApplicationsStartupListener implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		EntityResolverMap.intitalizeEntityResolverMaps();
		log.info("Institutional entity map init completed");
	}

}
