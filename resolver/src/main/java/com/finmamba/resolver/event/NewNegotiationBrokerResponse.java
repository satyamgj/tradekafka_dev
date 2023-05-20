package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.KanjiWorldDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewNegotiationBrokerResponse extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private KanjiWorldDto kanjiWorldDto;
	
	private int responseType;

	public NewNegotiationBrokerResponse(Object object, KanjiWorldDto kanjiWorldDto, int responseType) {
		super(object);
		this.kanjiWorldDto = kanjiWorldDto;
		this.responseType = responseType;
	}

}
