package com.tradekafka.ondc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.ondc.ConfirmedTransactions;

@Repository
public interface ConfirmRequestRepository extends JpaRepository<ConfirmedTransactions, Long>{

}
