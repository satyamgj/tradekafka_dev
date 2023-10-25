package com.finmamba.resolver.services;

import java.util.List;

import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.MatchingTable;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.transaction.LatestQuoteDetailRequest;
import com.finmamba.models.transaction.MarketDepthRequest;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.resolver.exception.EntityNotFound;

public interface TransactionServiceDal {
	/**
	 * To access transaction tables
	 */

	public List<QuoteRequestDto> saveRequest(QuoteRequestModel quoteRequestModel) throws EntityNotFound; // visited

	public List<InstitutionalEntity> findEligibleBrokers(long sourceId) throws EntityNotFound; // visited

	public List<InstitutionalEntity> findEligibleCounterparties(long source, Boolean isBroadcastToBrokers) throws EntityNotFound; // visited

	HttpResponseModel findActiveRequestsForInstitution(String institutionName, int page, int pageSize)
			throws EntityNotFound; // visited

	HttpResponseModel findInOutForTransaction(String transactionId, String institutionName) throws EntityNotFound; // visted
	
	HttpResponseModel findInOutForTransactionForBroker(String transactionId, String institutionName) throws EntityNotFound; 

	HttpResponseModel findInForInstitution(String institutionName, int page, int pageSize) throws EntityNotFound; // visited

	HttpResponseModel findConfirmedOrdersForInstitution(String institutionId, int page, int pageSize)
			throws EntityNotFound; // visited

	HttpResponseModel findConfirmedOrdersForBroker(String institutionId, int page, int pageSize) throws EntityNotFound; // visited
	
	HttpResponseModel findBuyInOrdersForBroker(String brokerId,int page, int pageSize) throws EntityNotFound;
	
	HttpResponseModel findSellInOrdersForBroker(String brokerId,int page, int pageSize) throws EntityNotFound;

	
	HttpResponseModel findDepthOfCcyPairByInstituionId(MarketDepthRequest marketDepthRequest);
	
	HttpResponseModel intiateMatchingAlgorithms(MatchingTable order);
	
	HttpResponseModel getLatestQuoteBySource(LatestQuoteDetailRequest latestQuoteDetailRequest);
	
	HttpResponseModel getBuyOrdersFromBroker(Long brokerId);
}
