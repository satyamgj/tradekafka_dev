package com.tradekafka.ondc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.ondc.MatchingTransactions;
import com.finmamba.entities.ondc.QuoteRequest;

@Repository
public interface MatchingTransactionRepository extends JpaRepository<MatchingTransactions, Long>{

	List<MatchingTransactions> findByBuyTxnId(QuoteRequest buyTransaction);
}
