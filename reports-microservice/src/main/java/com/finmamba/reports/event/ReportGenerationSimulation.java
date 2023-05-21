package com.finmamba.reports.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportGenerationSimulation extends ApplicationEvent {

	/**
	 * Not needed at the moment.
	 */
	private static final long serialVersionUID = 1L;

	public ReportGenerationSimulation(Object object) {
		super(object);
	}

}
