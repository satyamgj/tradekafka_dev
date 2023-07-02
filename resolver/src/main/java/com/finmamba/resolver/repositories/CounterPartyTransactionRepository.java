package com.finmamba.resolver.repositories;

import java.util.Collection;
import java.util.List;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

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
	
	List<CounterpartyTransactionDetails> findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
			InstitutionalEntity source,
			InstitutionalEntity destination,
			Boolean isRequestActive,
			TransactionDetail transactionId);
	
	List<CounterpartyTransactionDetails> findAllByDestinationIdAndEventTypeIn(InstitutionalEntity destination,
																		Collection<EventType> events,
																		
																		Pageable pageRequest);	
	
	List<CounterpartyTransactionDetails> findAllByDestinationIdAndEventTypeInAndIsRequestActive(InstitutionalEntity destination,
			Collection<EventType> events, Boolean isRequestActive);
	
	
	List<CounterpartyTransactionDetails> findAllBySourceIdAndEventTypeInAndIsRequestActive(InstitutionalEntity source,
			Collection<EventType> events, Boolean isRequestActive);
	
	List<CounterpartyTransactionDetails> findAllByDestinationId(InstitutionalEntity destination,
			Pageable pageRequest);	
	
	List<CounterpartyTransactionDetails> findByDestinationIdAndDirectionIdAndIsRequestActive(InstitutionalEntity destination,
			
			Direction direction,
			
			Boolean isActive,
			
			Pageable pageRequest
			
			);	
	
}
