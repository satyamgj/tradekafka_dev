package com.finmamba.resolver.operations;

import com.finmamba.entities.transaction.SuperInstitutionalEntity;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;

public interface InstitutionalEntityOperations {
	
	/*
	 * Insert IN_REQUEST operation
	 * Persists a new quote request in the database
	 * This method should be implemented by all realms
	 * 1 -> 2
	 */
	public SuperInstitutionalEntityDto insertDaddyRequest(DaddyDto daddyDto);
	
	/*	Insert OUT_REQUEST operation
	 * Persists a new quote request in the database
	 * This method should be implemented by all counterparty realms
	 * 2 -> 3
	 */
	public Boolean dispatchRequestToBrokers(SuperInstitutionalEntityDto entityDto);
	
	/*	Insert OUT_REQUEST operation
	 * Persists a new quote request in the database
	 * This method should be implemented by broker realms
	 * 2 -> 3
	 */
	public Boolean dispatchRequestToCounterparties(DaddyDto daddyDto, String callerRealName);
	
	/*	Insert IN_REQUEST operation
	 * Persists a new quote request in the database
	 * This method should be implemented by broker realms
	 * 2 -> 3
	 */
	public SuperInstitutionalEntityDto insertInRequest(SuperInstitutionalEntityDto entityDto);
	
	/*	Insert IN_REQUEST operation
	 * Persists a new quote request in the database
	 * This method should be implemented by counterparty realms
	 * 2 -> 3
	 */
	public SuperInstitutionalEntityDto insertInRequest(DaddyDto daddyDto);
	
	/*	Insert OUT_RESPONSE operation
	 * Persists a new order response in the database
	 * This method should be implemented by counterparty realms
	 * 3 -> 2 | 1 -> 2
	 */
	public SuperInstitutionalEntityDto insertOutResponse(DaddyDto daddyDto,String sendToRealmName);

	/*	Insert OUT_NEGOTIATION operation
	 * Persists a new order response in the database
	 * This method should be implemented by counterparty realms
	 * 3 -> 2 | 1 -> 2
	 */
	public SuperInstitutionalEntityDto insertOutNegotiation(DaddyDto daddyDto, String sendToRealmName);
	
	/*	Insert IN_RESPONSE operation
	 * Persists a new order response in the database
	 * This method should be implemented by counterparty realms
	 * 3 -> 2 | 1 -> 2
	 */
	public SuperInstitutionalEntityDto insertInResponse(DaddyDto daddyDto);

	/*	Insert IN_NEGOTIATION operation
	 * Persists a new order response in the database
	 * This method should be implemented by counterparty realms
	 * 3 -> 2 | 1 -> 2
	 */
	public SuperInstitutionalEntityDto insertInNegotiation(DaddyDto daddyDto);
	
	
	/*	Insert OUT_REJECT operation
	 * Persists a new order response in the database
	 * This method should be implemented by all realms
	 * isBrokerReject is unused in counter party realms
	 * 3 -> 2 | 1 -> 2
	 */
	public SuperInstitutionalEntityDto insertOutReject(DaddyDto daddyDto, boolean isBrokerReject,String sendToRealmName);
	
	/*	Insert IN_REJECT operation
	 * Persists a new order response in the database
	 * This method should be implemented by counterparty realms
	 * 3 -> 2 | 1 -> 2
	 */
	public SuperInstitutionalEntityDto insertInReject(DaddyDto daddyDto);
	public SuperInstitutionalEntityDto insertInReject(SuperInstitutionalEntityDto entityDto);
	
	public SuperInstitutionalEntityDto insertInNegotiation(SuperInstitutionalEntityDto entityDto);

	
	
	public SuperInstitutionalEntity getInstitutionalEntityInstance();
	
	public InstitutionalEntityOperations getInstitutionalEntityOperationsInstance();
	

}
