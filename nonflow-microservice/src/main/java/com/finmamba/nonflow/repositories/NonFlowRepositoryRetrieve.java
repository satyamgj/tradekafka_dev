package com.finmamba.nonflow.repositories;

import java.math.BigInteger;
import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.reports.NonFlowReport;
import com.finmamba.entities.transaction.NonFlow;

@Repository
public interface NonFlowRepositoryRetrieve extends JpaRepository<NonFlowReport, Long> {
	
	@Transactional
	  @Procedure(procedureName = "transaction.fn_get_nonflow_trade_report") 
	  public List<NonFlowReport> findByEntityIdAndCreateAtBetween(Integer institutionId, Date startDate,
	  Date endDate, Pageable pageable);
	 
}
