package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.TransactionDetail;

@Repository
public interface QuoteRequestRepository extends JpaRepository<TransactionDetail, Long> {

}
