package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.SanjayForexWorld;

@Repository
public interface SanjayForexWorldRepository extends JpaRepository<SanjayForexWorld, Long> {

	public SanjayForexWorld findByTransactionIdAndDestinationIdAndEventType(String transactionId, long destinationId,
			EventType eventType);

	public SanjayForexWorld findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(String transactionId,
			InstitutionalEntity destinationId, EventType eventType, boolean isRequestActive);

}
