package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.KotakWorld;

@Repository
public interface KotakWorldRepository extends JpaRepository<KotakWorld, Long> {
	// IN
//		@Query(value = "select * from transaction.kotakworld kw where kw.transaction_id = 'd769dd44e08751a5ca39b71cfc9fe648' and kw.event_type_id in (1,3)", nativeQuery = true)
//		public List<Daddy> findInForTransactionId(String transactionId, String institutionName);
	@Procedure(procedureName = "transaction.FIND_IN_EVENT_FOR_TRANSACTION_INSTITUTION")
	public List<KotakWorld> findInForTransactionId(String transactionId, String institutionName);

	// OUT
	@Procedure(procedureName = "transaction.FIND_OUT_EVENT_FOR_TRANSACTION_INSTITUTION")
	public List<KotakWorld> findOutForTransactionId(String transactionId, String institutionName);

	// Confirmed
	@Procedure(procedureName = "transaction.FIND_CONFIRM_ORDER_EVENT_FOR_TRANSACTION_INSTITUTION")
	public List<KotakWorld> findConfirmedOrderForTransactionId(String transactionId, String institutionName);

	// Confirmed
	@Procedure(procedureName = "transaction.FIND_REJECT_ORDER_EVENT_FOR_TRANSACTION_INSTITUTION")
	public List<KotakWorld> findRejectedOrderForTransactionId(String transactionId, String institutionName);
	
	@Procedure(procedureName = "transaction.FIND_AWAITING_EVENT_FOR_TRANSACTION_INSTITUTION")
	public List<KotakWorld> findAwaitingOrderForTransactionId(String transactionId, String institutionName);

	public KotakWorld findByTransactionIdAndDestinationIdAndEventType(String transactionId, long destinationId,
			EventType eventType);

	@Procedure(procedureName = "transaction.find_in_event_for_institution")
	public List<KotakWorld> getInEventForInstitution(String institutionName);

	public KotakWorld findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(String transactionId,
			InstitutionalEntity destinationId, EventType eventType, boolean isRequestActive);

}
