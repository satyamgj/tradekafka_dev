package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.DaddyDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewBrokerResponse extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient DaddyDto daddyDto;
	
	private String callerRealmName;
	
	private int responseType = -1;
	
	private String sendToRealmName;

	public NewBrokerResponse(Object object, DaddyDto daddyDto, String callerRealmName) {
		super(object);
		this.daddyDto = daddyDto;
		this.callerRealmName = callerRealmName;
		
	}
	
	public NewBrokerResponse(Object object, DaddyDto daddyDto, String callerRealmName, int responseType, String sendToRealmName) {
		super(object);
		this.daddyDto = daddyDto;
		this.callerRealmName = callerRealmName;
		this.responseType = responseType;
		this.sendToRealmName = sendToRealmName;
	}

}
