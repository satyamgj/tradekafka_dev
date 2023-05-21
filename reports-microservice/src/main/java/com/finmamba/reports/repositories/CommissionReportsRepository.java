package com.finmamba.reports.repositories;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.KanjiWorld;

@Repository
public interface CommissionReportsRepository extends JpaRepository<KanjiWorld, Long> {

	// TODO: Select only the columns you need for reports.
	
	@Transactional
	@Procedure(procedureName = "transaction.commission_report_for_institution_between_dates")
	public List<KanjiWorld> getCommissionReportFromEntity(
			BigInteger reportId, 
			BigInteger institutionId,
			String startDate,
			String endDate, Pageable pageable);
}
