package com.finmamba.resolver.services;

import java.util.List;
import java.util.Map;

import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.RequestQuote;
import com.finmamba.entities.transaction.TransactionDetail;
import com.finmamba.models.dto.InstitutionalEntityDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.request.RecordProcessedStatusDetail;
import com.finmamba.models.request.UserProfileModel;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.models.transaction.SourceDestination;
import com.finmamba.resolver.exception.EntityNotFound;

public interface TransactionServiceDal {
	/**
	 * To access transaction tables
	 */

	public List<QuoteRequestDto> saveRequest(QuoteRequestModel quoteRequestModel) throws EntityNotFound;
	
	public List<InstitutionalEntity> findEligibleBrokers(long sourceId) throws EntityNotFound;

	
	public List<RequestQuote> saveRequestFromBroker(RequestQuote request) throws EntityNotFound, IllegalArgumentException;
	public RequestQuote saveCounterpartyResponse(RequestQuote request) throws EntityNotFound, IllegalArgumentException;
	public RequestQuote saveBrokerResponse(RequestQuote request) throws EntityNotFound, IllegalArgumentException;
	
	
	public RequestQuote findQuoteRequestById(long requestId) throws EntityNotFound;
	public List<RequestQuote> findQuoteRequestByUserId(long userId, boolean requestStatusFlag) throws EntityNotFound;
	public List<RequestQuote> findQuoteRequestByBrokerId(long brokerId) throws EntityNotFound;
	public List<RequestQuote> findQuoteRequestForBrokerId(long brokerId, boolean recordProcessedStatus) throws EntityNotFound;
	public List<RequestQuote> findExpiredQuoteRequestForBrokerId(long brokerId) throws EntityNotFound;
	
	public boolean forwardRequestToCounterparties(long requestId) throws EntityNotFound;

	public InstitutionalEntity findInstitutionEntity(long institutionalEntityId) throws EntityNotFound;
	
	public List<InstitutionalEntity> findEligibleCounterparties(SourceDestination sourceDestination) throws EntityNotFound;
	
	
	public boolean expireOrderRequest(Long parentId, String reasonForExpiry) throws EntityNotFound, IllegalArgumentException;
	
	public boolean expireOrderRequest(String parentId, String reasonForExpiry) throws EntityNotFound, IllegalArgumentException;
	
	public boolean findStatusOfTransaction(long requestId) throws EntityNotFound ;
	
	
	public InstitutionalEntity findInstitutionalEntityFromId(long id) throws EntityNotFound;

	public Boolean updateRecordProcessedStatus(RecordProcessedStatusDetail processedStatusDetail) throws EntityNotFound;

	/****************************************** FIND **************************************/
	//List<DaddyDto> findActiveRequestsForInstitution(String institutionName, int page, int pageSize) throws EntityNotFound;
	Map<String, Object> findActiveRequestsForInstitution(String institutionName, int page, int pageSize) throws EntityNotFound;
	Map<String, Object> findConfirmedOrdersForInstitution(String institutionName, int page, int pageSize) throws EntityNotFound;
	
	//Map<String, List<KotakWorldDto>> findInOutForTransaction(String transactionId, String institutionName) throws EntityNotFound;
	Map<String, List<SuperInstitutionalEntityDto>> findInOutForTransaction(String transactionId, String institutionName) throws EntityNotFound;
	
	
	Map<String, Object> findInForInstitution(String institutionName, int page, int pageSize) throws EntityNotFound;
	
	
	List<RequestQuote> findInTransitRequestsForInstitution(long institutionId) throws EntityNotFound;
	List<RequestQuote> findInTransitRequestsForTransaction(Double institutionId, String transactionId) throws EntityNotFound;
	List<RequestQuote> findExpiredRequestsForInstitution(long institutionId) throws EntityNotFound;
	List<RequestQuote> findConfirmedRequestsForInstitution(long institutionId) throws EntityNotFound;
	public InstitutionalEntityDto findInstitutionalEntityFromUserProfile(UserProfileModel userProfileModel) throws EntityNotFound ;

	public TransactionDetail findDaddyRequestByTransactionId(String transactionId) ;



















}
