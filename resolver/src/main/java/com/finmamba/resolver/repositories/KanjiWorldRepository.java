package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.KanjiWorld;

@Repository
public interface KanjiWorldRepository extends JpaRepository<KanjiWorld, Long> {
	
	public KanjiWorld findByTransactionIdAndDestinationIdAndEventType(String transactionId, long destinationId, EventType eventType);
	public KanjiWorld findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(String transactionId, InstitutionalEntity destinationId, EventType eventType, boolean isRequestActive);
	
}
