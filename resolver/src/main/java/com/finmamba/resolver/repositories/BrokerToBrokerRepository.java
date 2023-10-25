package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.BrokerToBrokerTransactionDetails;
import com.finmamba.entities.transaction.TransactionDetail;

@Repository
public interface BrokerToBrokerRepository extends JpaRepository<BrokerToBrokerTransactionDetails, Long> {

	List<BrokerToBrokerTransactionDetails> findAllByDirectionIdAndBrokerIdAndIsActiveOrderByLastTransactionAtDesc(
			Direction direction,
			Long broker,
			Boolean isActive
			);

	BrokerToBrokerTransactionDetails findByBrokerIdAndSourceBrokerIdAndTransactionId(
			Long brokerId,
			Long SourceId,
			TransactionDetail transaction
			);
}
