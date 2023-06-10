package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.CounterpartyTransactionDetails;

@Repository
public interface CounterPartyTransactionRepository extends JpaRepository<CounterpartyTransactionDetails, Long>{

	List<CounterpartyTransactionDetails> findAllByDestinationIdOrSourceIdAndIsRequestActiveAndTransactionId(
									InstitutionalEntity destination,
									InstitutionalEntity source,
									Boolean isRequestActive,
									String transactionId);
	
	CounterpartyTransactionDetails findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId(
			InstitutionalEntity source,
			InstitutionalEntity destination,
			Boolean isRequestActive,
			String transactionId);
	
	List<CounterpartyTransactionDetails> findAllByDestionationIdAndEventTypeIn(InstitutionalEntity destination,
																		List<EventType> events,
																		Pageable pageRequest);	
	
}
