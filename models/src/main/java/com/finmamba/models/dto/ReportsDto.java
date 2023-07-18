package com.finmamba.models.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReportsDto {

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
