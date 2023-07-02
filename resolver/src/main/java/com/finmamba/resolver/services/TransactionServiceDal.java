package com.finmamba.resolver.services;

import java.util.List;

import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.transaction.QuoteRequestModel;
import com.finmamba.resolver.exception.EntityNotFound;

public interface TransactionServiceDal {
	/**
	 * To access transaction tables
	 */

	public List<QuoteRequestDto> saveRequest(QuoteRequestModel quoteRequestModel) throws EntityNotFound; // visited

	public List<InstitutionalEntity> findEligibleBrokers(long sourceId) throws EntityNotFound; // visited

	public List<InstitutionalEntity> findEligibleCounterparties(long source) throws EntityNotFound; // visited

	HttpResponseModel findActiveRequestsForInstitution(String institutionName, int page, int pageSize)
			throws EntityNotFound; // visited

	HttpResponseModel findInOutForTransaction(String transactionId, String institutionName) throws EntityNotFound; // visted

	HttpResponseModel findInForInstitution(String institutionName, int page, int pageSize) throws EntityNotFound; // visited

	HttpResponseModel findConfirmedOrdersForInstitution(String institutionId, int page, int pageSize)
			throws EntityNotFound; // visited

	HttpResponseModel findConfirmedOrdersForBroker(String institutionId, int page, int pageSize) throws EntityNotFound; // visited
	
	HttpResponseModel findInOrdersForBroker(String brokerId,int page, int pageSize) throws EntityNotFound;

}
