package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.AxisWorld;

@Repository
public interface AxisWorldRepository extends JpaRepository<AxisWorld, Long> {
	
	public AxisWorld findByTransactionIdAndDestinationIdAndEventType(String transactionId, long destinationId, EventType eventType);
	public AxisWorld findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(String transactionId, InstitutionalEntity destinationId, EventType eventType, boolean isRequestActive);
	
}
