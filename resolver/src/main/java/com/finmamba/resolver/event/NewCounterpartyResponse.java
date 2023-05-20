package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.DaddyDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCounterpartyResponse extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DaddyDto daddyDto;
	
	private int responseType;
	
	private String callerRealmName;
	
	private String sendToRealmName;

	public NewCounterpartyResponse(Object object, DaddyDto daddyDto, int responseType, String callerRealmName,String sendToRealmName) {
		super(object);
		this.callerRealmName = callerRealmName;
		this.daddyDto = daddyDto;
		this.responseType = responseType;
		this.sendToRealmName = sendToRealmName;
	}

}
