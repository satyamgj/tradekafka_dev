package com.finmamba.resolver.services;

import java.util.List;

import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.models.transaction.ConfirmRequest;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.exception.EntityNotFound;

public interface TransactionService {
	/*
	 * Services to process requests and responses
	 */
	public List<QuoteRequestDto> processQuoteRequest(QuoteRequestModel quoteRequestModel) throws EntityNotFound; //visited
	
	
	public boolean processBrokerRequest(TransactionRequest requestBrokerDispatchRequest) throws EntityNotFound; //visited

	public boolean processCounterpartyResponse(TransactionRequest request, int responseType) throws EntityNotFound; //visited
	
	public boolean confirmRequest(ConfirmRequest confirmRequest);

	public HttpResponseModel findActiveRequestsForInstitution(String instituitionId, int page, int pageSize); //visited

	public HttpResponseModel findInOutForTransaction(String transactionName, String institutionId); //visited

	public HttpResponseModel findInRequestsForInstitution(String institutionId, int page, int pageSize); //visited
	
	public HttpResponseModel findInRequestsForBroker(String institutionId, int page, int pageSize); //visited

	
	public InstitutionalEntityDto findInstitutionalEntityFromUserProfile(UserProfileModel userProfileModel); //visited
	
	
	public HttpResponseModel findConfirmedOrdersForInstitution(String institutionName, int page, int pageSize); //visited
	
	public HttpResponseModel findConfirmedOrdersForBroker(String institutionName, int page, int pageSize); //visited

	
}
