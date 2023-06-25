package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.transaction.ConfirmRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewConfirmRequest extends ApplicationEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient ConfirmRequest confirmRequest;
	


	public NewConfirmRequest(Object object, ConfirmRequest confirmRequest) {
		super(object);
		this.confirmRequest = confirmRequest;
		
	}
	

}
