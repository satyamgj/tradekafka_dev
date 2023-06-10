package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.transaction.TransactionRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewBrokerRequest extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient TransactionRequest brokerDispatchRequest;

	public NewBrokerRequest(Object object, TransactionRequest brokerDispatchRequest) {
		super(object);
		this.brokerDispatchRequest = brokerDispatchRequest;
	}

}
