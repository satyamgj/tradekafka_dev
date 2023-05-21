package com.finmamba.reports.repositories;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.TransactionDetail;

@Repository
public interface TradeReportsRepository extends JpaRepository<TransactionDetail, Long> {

	@Transactional
	@Procedure(procedureName = "transaction.find_confirm_order_event_for_institution_between_dates")
	public List<TransactionDetail> getTradeReportFromEntity(
			BigInteger reportId, 
			BigInteger institutionId,
			String startDate,
			String endDate, Pageable pageable);
	
}
