package com.finmamba.nonflow.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonFlowSimulation extends ApplicationEvent {

	/**
	 * Not needed at the moment.
	 */
	private static final long serialVersionUID = 1L;

	public NonFlowSimulation(Object object) {
		super(object);
	}

}
