package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.transaction.TransactionRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCounterpartyResponse extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TransactionRequest request;

	private int responseType;
	
	public NewCounterpartyResponse(Object object, TransactionRequest request, int responseType) {
		super(object);
		this.request = request;
		this.responseType = responseType;
	}

}
