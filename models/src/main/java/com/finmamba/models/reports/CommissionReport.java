package com.finmamba.models.reports;

import java.math.BigInteger;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class CommissionReport {

	@NonNull
	private String startDate;

	@NonNull
	private String endDate;

	@NonNull
	private BigInteger reportId;

	@NonNull
	private Long pageSize;
	
	// Page number is called offset
	@NonNull
	private Long offset;

	@NonNull
	private String sortOrder;
	
	@NonNull
	private String sortField;
	
	@NonNull
	private BigInteger entityId;
}
