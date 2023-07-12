package com.finmamba.nonflow.repositories;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.NonFlow;

@Repository
public interface NonFlowRepository extends JpaRepository<NonFlow, Long> {

	// TODO: Select only the columns you need for reports.
	// Query for All
	@Transactional
	@Procedure(procedureName = "transaction.get_nonflow_requests")
	public List<NonFlow> getNonFlowRequest(
			BigInteger institutionId,
			String startDate,
			String endDate, Pageable pageable);
	}
