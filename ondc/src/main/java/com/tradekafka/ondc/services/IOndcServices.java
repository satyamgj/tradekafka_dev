package com.tradekafka.ondc.services;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.ondc.ConfirmRequest;
import com.finmamba.models.ondc.QuoteRequest;
import com.finmamba.models.ondc.SellerCatalogue;

public interface IOndcServices {

	public HttpResponseModel quoteRequest(QuoteRequest quoteRequest);
	public HttpResponseModel confirmRequest(ConfirmRequest confirmRequest);
	public HttpResponseModel fetchMyRequests(Long userId);
	public HttpResponseModel fetchMatchedRequests(Long quoteId);
	public HttpResponseModel fetchConfirmedQuotes(Long userId);
	public HttpResponseModel addSellerCatalogue(SellerCatalogue catalogue);
	public HttpResponseModel fetchSellerCatalogue(Long userId);
	public void broadcastToSellers(Currency buyCcy, Currency sellCcy, com.finmamba.entities.ondc.QuoteRequest quoteRequest);
	public HttpResponseModel fetchRequestedQuotes(Long userId);
	public HttpResponseModel alreadyRespondedToQuote(Long userId, Long quoteId);
}

