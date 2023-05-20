package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.DaddyDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDaddyRequest extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String eventCode  = "NEW_DADDY_REQUEST";

	private DaddyDto daddyDto;

	public NewDaddyRequest(Object object, DaddyDto daddyDto) {
		super(object);
		this.daddyDto = daddyDto;
	}

}
