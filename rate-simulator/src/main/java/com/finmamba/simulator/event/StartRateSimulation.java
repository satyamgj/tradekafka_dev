package com.finmamba.simulator.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartRateSimulation extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StartRateSimulation(Object object) {
		super(object);
	}

}
