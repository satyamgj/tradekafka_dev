package com.finmamba.models.reports;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;


import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SearchNonFlowDto {

	@NonNull
	private String startDate;

	@NonNull
	private String endDate;

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