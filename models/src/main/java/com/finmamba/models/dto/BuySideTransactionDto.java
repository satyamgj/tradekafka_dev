package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BuySideTransactionDto implements Serializable{private static final long serialVersionUID = 1L;
private Long id;

private ProductDto product;

private DirectionDto direction;

private CurrencyDto baseCurrency;

private CurrencyDto quoteCurrency;

private BigDecimal buyAmount;

private BigDecimal sellAmount;

private BigDecimal destinationOutright;

private TenorDto tenorUnit;

private InstitutionalEntityDto source;

private InstitutionalEntityDto destinationId;

private boolean isRequestActive;

private TransactionDto transactionId;

private EventTypeDto eventType;
	
private UserDto createdBy;

@JsonProperty("spread")
private BigDecimal buySideSpread;

@JsonProperty("commission")
private BigDecimal calculatedBuyCommission;

@JsonProperty("commissionMode")
private Boolean brokerCommissionIsPip;

@JsonProperty("brokerageCurrency")
private CurrencyDto buySideBrokerageCurrency;

@JsonProperty("brokerageBaseCurrency")
private CurrencyDto buySideBrokerageBaseCurrency;

@JsonProperty("brokerageBaseAmount")
private BigDecimal buySideBaseAmount;

@JsonProperty("brokerageConversionRate")
private BigDecimal buySideConversionRate;

private Instant createdAt;
}
