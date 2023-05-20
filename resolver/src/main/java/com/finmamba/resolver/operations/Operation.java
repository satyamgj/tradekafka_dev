package com.finmamba.resolver.operations;

import com.finmamba.models.dto.QuoteRequestDto;

public interface Operation {

	/*
	 * Insert IN_REQUEST operation
	 * Persists a new quote request in the database
	 * This method should be implemented by all realms
	 * 1 -> 2
	 */
	public QuoteRequestDto insertQuoteRequest(QuoteRequestDto quoteRequestDto);
}
