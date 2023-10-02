package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.transaction.TransactionBrokerDetails;
import com.finmamba.entities.transaction.TransactionDetail;

@Repository
public interface TransactionBrokerDetailRepository extends JpaRepository<TransactionBrokerDetails, Long> {
	TransactionBrokerDetails findByTransactionIdAndBrokerId(TransactionDetail transaction, Long brokerId);

	List<TransactionBrokerDetails> findAllByBrokerIdAndDirectionIdOrderByLastTransactionAtDesc(Long brokerId,
			Direction direction, Pageable pageRequest);
	
	Integer countByBrokerIdAndDirectionId(Long brokerId,
			Direction direction);

}
