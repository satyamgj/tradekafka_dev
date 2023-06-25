package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.TransactionDetail;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionDetail, Long>, PagingAndSortingRepository<TransactionDetail, Long> {

	TransactionDetail findByIdAndIsConfirmed(Long Id, Boolean isConfirmed);
	
	TransactionDetail findByTransactionId(String transactionId);
	
	List<TransactionDetail> findAllBySourceAndIsConfirmed(InstitutionalEntity source, Boolean isConfirmed, Pageable pageRequest);
	
	int countBySourceAndIsConfirmed(InstitutionalEntity source, Boolean isConfirmed);
	
}
