package com.tradekafka.ondc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.ondc.QuoteRequest;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.transaction.User;

@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long>{
	List<QuoteRequest> findByBuyUserId(User userId);
	List<QuoteRequest> findByBuyUserIdAndIsConfirmed(User user, Boolean isConfirmed);
	List<QuoteRequest> findByDirectionIdAndIsConfirmedAndResponseYN(Direction direction, Boolean isConfirmed, String responseYN);
	Boolean existsByResponseToAndBuyUserId(QuoteRequest respondedQuote, User buyUserId);
}
