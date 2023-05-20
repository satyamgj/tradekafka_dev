package com.finmamba.nonflow;

import java.math.BigInteger;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class NonFlowRequest {

	@NonNull
	private String startDate;

	@NonNull
	private String endDate;

	@NonNull
	private BigInteger nonFlowId;

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
