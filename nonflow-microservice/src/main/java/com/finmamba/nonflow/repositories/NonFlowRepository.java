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
public interface NonFlowRepository extends JpaRepository<NonFlow, Long> {

	// TODO: Select only the columns you need for reports.
	// Query for All

	@Transactional
	@Query(value = "select * from transaction.nonflow_transaction_details t where created_at BETWEEN :startDate AND :endDate"
			+ " AND entity_id = :entityId", nativeQuery = true)
	public List<NonFlow> getNonFlowRequest(@Param("entityId") BigInteger entityId,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

	/*
	 * @Query(value = "select * from transaction.confirmed_order_details where " +
	 * "created_at BETWEEN :startDate AND :endDate AND " +
	 * " is_non_flow_entry = :nonFlow AND ( buy_side_id = :institutionalId OR sell_side_id = :institutionalId) "
	 * , nativeQuery = true)
	 */

	/*
	 * public List<NonFlow> findByEntityIdAndCreateAtBetween(
	 * 
	 * Date startDate, Date endDate, Pageable pageable);
	 */
	 
}
