package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.IciciWorld;

@Repository
public interface IciciWorldRepository extends JpaRepository<IciciWorld, Long> {
	
	public IciciWorld findByTransactionIdAndDestinationIdAndEventType(String transactionId, long destinationId, EventType eventType);
	public IciciWorld findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(String transactionId, InstitutionalEntity destinationId, EventType eventType, boolean isRequestActive);
	
}
