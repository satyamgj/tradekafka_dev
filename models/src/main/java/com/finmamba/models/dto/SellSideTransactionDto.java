package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SellSideTransactionDto implements Serializable{private static final long serialVersionUID = 1L;
private Long id;


private BigDecimal destinationOutright;

private TenorDto tenorUnit;

private InstitutionalEntityDto source;

private InstitutionalEntityDto destinationId;

private boolean isRequestActive;

private TransactionDto transactionId;

private EventTypeDto eventType;
	
private UserDto createdBy;

@JsonProperty("spread")
private BigDecimal sellSideSpread;

@JsonProperty("commission")
private BigDecimal calculatedSellCommission;

@JsonProperty("commissionMode")
private Boolean brokerCommissionIsPip;

@JsonProperty("brokerageCurrency")
private CurrencyDto sellSideBrokerageCurrency;

@JsonProperty("brokerageBaseCurrency")
private CurrencyDto sellSideBrokerageBaseCurrency;

@JsonProperty("brokerageBaseAmount")
private BigDecimal sellSideBaseAmount;

@JsonProperty("brokerageConversionRate")
private BigDecimal sellSideConversionRate;

private Instant createdAt;

//@JsonProperty("direction")
//private DirectionDto direction;
}
