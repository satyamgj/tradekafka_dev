package com.finmamba.reports.repositories;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.reports.Reports;
import com.finmamba.entities.transaction.ConfirmedOrderDetails;

@Repository
public interface TradeReportsRepository extends JpaRepository<Reports, Long> {

	/*
	 * @Transactional
	 * 
	 * @Query(value = "select * from transaction.confirmed_order_details where " +
	 * "created_at BETWEEN :startDate AND :endDate AND " +
	 * " is_non_flow_entry = :nonFlow AND ( buy_side_id = :institutionalId OR sell_side_id = :institutionalId) "
	 * , nativeQuery = true) public List<ConfirmedOrderDetails>
	 * getAllBetweenDates(@Param("startDate") Date startDate,
	 * 
	 * @Param("endDate") Date endDate, @Param("nonFlow") Boolean nonFlow,
	 * 
	 * @Param("institutionalId") BigInteger institutionalId, Pageable pageable);
	 */
	
	
	
	@Procedure(procedureName =
	 "transaction.fn_get_trade_report_table") public
	 List<Reports> getReportFromEntity(Integer reportId,
	 Integer institutionId, Date startDate, Date endDate, Pageable
	 pageable);
	
	/*
	 * @Procedure(procedureName =
	 * "transaction.find_confirm_order_event_for_institution_between_dates") public
	 * List<ConfirmedOrderDetails> getTradeReportFromEntity( BigInteger reportId,
	 * BigInteger institutionId, String startDate, String endDate, Pageable
	 * pageable);
	 */
	
}
