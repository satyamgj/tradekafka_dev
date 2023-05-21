package com.finmamba.resolver.operations;

import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;

public interface Operation {

//	/*
//	 * Insert IN_REQUEST operation
//	 * Persists a new quote request in the database
//	 * This method should be implemented by all realms
//	 * 1 -> 2
//	 */
//	public QuoteRequestDto insertQuoteRequest(QuoteRequestDto quoteRequestDto); 
	
	/*	Insert OUT_REQUEST operation
	 * Persists a new quote request in the database
	 * This method should be implemented by all counterparty realms
	 * 2 -> 3
	 */
	public Boolean dispatchRequestToBrokers(QuoteRequestDto entityDto);
}
