package com.finmamba.resolver.repositories;

import java.util.Collection;
import java.util.List;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.entities.transaction.TransactionDetail;

@Repository
public interface CounterPartyTransactionRepository extends JpaRepository<CounterpartyTransactionDetails, Long>, PagingAndSortingRepository<CounterpartyTransactionDetails, Long>{

	List<CounterpartyTransactionDetails> findAllByDestinationIdOrSourceIdAndIsRequestActiveAndTransactionId(
									InstitutionalEntity destination,
									InstitutionalEntity source,
									Boolean isRequestActive,
									TransactionDetail transactionId);
	
	List<CounterpartyTransactionDetails> findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
			InstitutionalEntity source,
			InstitutionalEntity destination,
			Boolean isRequestActive,
			TransactionDetail transactionId);
	
	List<CounterpartyTransactionDetails> findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionIdOrderByIdDesc(
			InstitutionalEntity source,
			InstitutionalEntity destination,
			Boolean isRequestActive,
			TransactionDetail transactionId);
	
	List<CounterpartyTransactionDetails> findAllByDestinationIdAndEventTypeIn(InstitutionalEntity destination,
																		Collection<EventType> events,
																		
																		Pageable pageRequest);	
	
	List<CounterpartyTransactionDetails> findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActive(InstitutionalEntity destination,
			Collection<EventType> events,
			TransactionDetail transaction,
			Boolean isRequestActive);
	
	List<CounterpartyTransactionDetails> findAllByDestinationIdAndEventTypeInAndTransactionIdAndIsRequestActiveAndSourceIdNot
	(InstitutionalEntity destination,
			Collection<EventType> events,
			TransactionDetail transaction,
			Boolean isRequestActive,
			InstitutionalEntity source);
	
	
	List<CounterpartyTransactionDetails> findAllBySourceIdAndEventTypeInAndTransactionIdAndIsRequestActive(InstitutionalEntity source,
			Collection<EventType> events,
			TransactionDetail transaction,
			Boolean isRequestActive);
	
	List<CounterpartyTransactionDetails> findAllByDestinationId(InstitutionalEntity destination,
			Pageable pageRequest);	
	
	List<CounterpartyTransactionDetails> findByDestinationIdAndDirectionIdAndIsRequestActiveAndEventTypeIn(InstitutionalEntity destination,
			Direction direction,
			Boolean isActive,
			Collection<EventType> events,
			Pageable pageRequest
			);	
	
	long countByDestinationIdAndDirectionIdAndIsRequestActiveAndEventTypeIn(InstitutionalEntity destination,
			Direction direction,
			Boolean isActive,
			Collection<EventType> events
			);	
	
	
	List<CounterpartyTransactionDetails> findByDestinationIdAndSellCcyAndBuyCcyAndDirectionIdAndIsRequestActiveAndEventTypeIn(
						InstitutionalEntity destination,
						Currency sellSide,
						Currency buySide,
						Direction direction,
						Boolean isActive,
						Collection<EventType> events
				);

	List<CounterpartyTransactionDetails> findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionIdAndEventTypeInOrderByIdDesc(
			InstitutionalEntity source,
			InstitutionalEntity destination,
			Boolean isRequestActive,
			TransactionDetail transactionId,
			Collection<EventType> events
			);

}
