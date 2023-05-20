package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.Daddy;

@Repository
public interface DaddyRepository extends JpaRepository<Daddy, Long> {
//	@Query(value = "select *  from transaction.daddy d "
//			+ "where d.transaction_id in (select distinct kw.transaction_id from transaction.kotakworld kw)", nativeQuery = true)
//	public List<Daddy> findActiveRequestForInstitutionsWithValidity(@Param("institutionId") long institutionId,
//			@Param("recordStatus") long recordStatus);
	
	@Procedure(procedureName = "TRANSACTION.FIND_ACTIVE_ORDERS_FOR_INSTITUTION")
	public List<Daddy> findActiveRequestForInstitutionsWithValidity(String institutionName);

	// IN
//	@Query(value = "select * from transaction.kotakworld kw where kw.transaction_id = 'd769dd44e08751a5ca39b71cfc9fe648' and kw.event_type_id in (1,3)", nativeQuery = true)
//	public List<Daddy> findInForTransactionId(String transactionId, String institutionName);
	@Procedure(procedureName = "transaction.FIND_IN_EVENT_FOR_TRANSACTION_INSTITUTION")
	public List<Daddy> findInForTransactionId(String transactionId, String institutionName);
	
	// OUT
	@Procedure(procedureName = "transaction.FIND_OUT_EVENT_FOR_TRANSACTION_INSTITUTION")
	public List<Daddy> findOutForTransactionId(String transactionId, String institutionName);
	
	public Daddy findByTransactionId(String transactionId);
	
	@Procedure(procedureName = "transaction.GET_SORT_ORDER_FOR_INSTITUTION")
	public List<String> getSortOrderForInstitutionName(String institutionName);
	
	// Confirmed
	@Procedure(procedureName = "transaction.FIND_CONFIRM_ORDER_EVENT_FOR_INSTITUTION")
	public List<Daddy> findConfirmedOrderForInstitution(String institutionName);
	
	
	
}
