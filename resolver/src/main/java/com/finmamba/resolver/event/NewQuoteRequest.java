package com.finmamba.resolver.event;

import org.springframework.context.ApplicationEvent;

import com.finmamba.models.dto.QuoteRequestDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewQuoteRequest extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private QuoteRequestDto quoteRequestDto;

	public NewQuoteRequest(Object object, QuoteRequestDto quoteRequestDto) {
		super(object);
		this.quoteRequestDto = quoteRequestDto;
	}

}
