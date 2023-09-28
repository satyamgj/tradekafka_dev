package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.MatchingTable;

@Repository
public interface MatchingRepository extends JpaRepository<MatchingTable, Long> {

	List<MatchingTable> findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyId(Long destination,
			Long direction, Long buyCurrencyId, Long sellCurrencyId);

	List<MatchingTable> findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventType(Long destination,
			Long direction, Long buyCurrencyId, Long sellCurrencyId, Long eventTypeId);

	List<MatchingTable> findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventTypeAndTransactionId(
			Long destination, Long direction, Long buyCurrencyId, Long sellCurrencyId, Long eventTypeId,
			Long transactionId);
}
