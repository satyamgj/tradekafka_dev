package com.finmamba.reports.repositories;

import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.ConfirmedOrderDetails;

@Repository
public interface TradeReportsRepository extends JpaRepository<ConfirmedOrderDetails, Long> {

	@Transactional
	@Query(value = "select * from transaction.confirmed_order_details where created_at BETWEEN :startDate AND :endDate AND "
			+ " non_flow_entry = :nonFlow", nativeQuery = true)
	public List<ConfirmedOrderDetails> getAllBetweenDates(@Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param ("nonFlow") Boolean nonFlow, Pageable pageable);

	/*
	 * @Procedure(procedureName =
	 * "transaction.find_confirm_order_event_for_institution_between_dates") public
	 * List<ConfirmedOrderDetails> getTradeReportFromEntity( BigInteger reportId,
	 * BigInteger institutionId, String startDate, String endDate, Pageable
	 * pageable);
	 */

	
}
