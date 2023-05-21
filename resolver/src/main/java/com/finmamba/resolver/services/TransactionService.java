package com.finmamba.resolver.services;

import java.util.List;
import java.util.Map;

import com.finmamba.entities.transaction.RequestQuote;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.models.transaction.FXRequestModel;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.resolver.exception.EntityNotFound;

public interface TransactionService {
	/*
	 * Services to process requests and responses
	 */
	public List<QuoteRequestDto> processQuoteRequest(QuoteRequestModel quoteRequestModel) throws EntityNotFound;
	
	
	public boolean processBrokerRequest(DaddyDto request, String callerRealmName) throws EntityNotFound;

	
	
	
	
	
	
	
	public boolean processCounterpartyResponse(DaddyDto request, int responseType, String callerRealmName, String sendToRealmName) throws EntityNotFound;
	public boolean processBrokerResponse(DaddyDto request, String callerRealmName, int responseType, String sendToRealmName ) throws EntityNotFound;
	public boolean processBrokerResponse(DaddyDto request, String callerRealmName) throws EntityNotFound;
	public boolean processNegotiationResponse(DaddyDto negotiation, int responseType) throws EntityNotFound;
	public boolean processNegotiationBrokerResponse(DaddyDto negotiation, int responseType) throws EntityNotFound;
	
	/*
	 * Service to forward trasaction data between different parties
	 */
	public boolean forwardRequestToCounterparties(List<RequestQuote> counterPartyRequests) throws EntityNotFound ;
	public boolean forwardRequestToBroker(FXRequestModel fxModel, List<RequestQuote> requests) throws EntityNotFound;
	/*
	 * Data provider services
	 */
	//public List<DaddyDto> findActiveRequestsForInstitution(String institutionName, int page, int pageSize);
	public Map<String, Object> findActiveRequestsForInstitution(String institutionName, int page, int pageSize);
	public Map<String, Object> findConfirmedOrdersForInstitution(String institutionName, int page, int pageSize);
	
	public Map<String, List<SuperInstitutionalEntityDto>> findInOutForTransaction(String transactionName, String institutionName);
	public DaddyDto findDaddyRequestByTransactionId(String transactionId);
	
//	public Map<String, List<RequestQuote>> findInTransitRequestsForInstitution(long institutionId);
	public List<RequestQuote> findInTransitRequestsForInstitution(long institutionId);
	public List<RequestQuote> findExpiredRequestsForInstitution(long institutionId);
	public List<RequestQuote> findConfirmedRequestsForInstitution(long institutionId);
	public List<RequestQuote> findTrailForTransaction(Long institutionId, String transactionId);
	public InstitutionalEntityDto findInstitutionalEntityFromUserProfile(UserProfileModel userProfileModel);
	
	public Map<String, Object> findInRequestsForInstitution(String institutionName, int page, int pageSize);
	/*
	 * Perform actions of transactions
	 */
	public boolean expireOrder(String transactionId, String reasonForExpiry);
	
}
