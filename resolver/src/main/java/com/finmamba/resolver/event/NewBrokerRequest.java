package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.DaddyDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewBrokerRequest extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient DaddyDto daddyDto;
	
	private transient String callerRealmName;

	public NewBrokerRequest(Object object, DaddyDto daddyDto, String callerRealmName) {
		super(object);
		this.daddyDto = daddyDto;
		this.callerRealmName = callerRealmName;
	}

}
