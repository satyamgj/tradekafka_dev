package com.finmamba.models.reports;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

import org.springframework.lang.NonNull;

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
public class NonFlowReportDto {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private String brokerName;
	
	private Integer transactionId;
	
	private String sourceName;
	
	private Integer counterpartyTransactionId;
	
	private String name;
	
	private Date createdAt;
	
	private Double spread;

	private String direction;
	
	private String brokerageCcy;
	
	private Double amount;
	
	private String brokerageBaseCcy;
	
	private Double baseAmount;

	private Double commission;
	
	private Double conversionRate;

	private String bankBuyCcyId;

	private String bankSellCcyId;

	private Double clientOutright;

	private Boolean brokerCommissionModeIsPip;

	private String instrumentType;
	
	private Long totalCount;
	
	private int httpStatus;

}

