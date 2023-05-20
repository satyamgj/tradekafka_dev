package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.KotakWorldDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewNegotiationResponse extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private KotakWorldDto kotakWorldDto;
	
	private int responseType;

	public NewNegotiationResponse(Object object, KotakWorldDto kotakWorldDto, int responseType) {
		super(object);
		this.kotakWorldDto = kotakWorldDto;
		this.responseType = responseType;
	}

}
