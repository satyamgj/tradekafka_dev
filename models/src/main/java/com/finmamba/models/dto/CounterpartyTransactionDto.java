package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CounterpartyTransactionDto implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	
	private TransactionDto transactionId; //While fetching its null
	
//	private ProductDto product;
	
//	private DirectionDto buySellDirection;
	
//	private CurrencyDto baseCurrency;
//	
//	private CurrencyDto quoteCurrency;
	
//	private BigDecimal buyAmount;
//
//	private BigDecimal sellAmount;
	
	private BigDecimal clientOutright;

//	private TenorDto tenorUnit;
	
	private InstitutionalEntityDto source;
	
	private boolean isRequestActive;
	
	private EventTypeDto eventType;
		
	private UserDto createdBy;
	
	private BigDecimal buySideSpread;
	
	private BigDecimal sellSideSpread;
	
	private BigDecimal calculatedBuyCommission;
	
	private BigDecimal calculatedSellCommission;
	
	private Boolean brokerCommissionIsPip;
	
	private BigDecimal brokerCommission;

	private CurrencyDto buySideBrokerageCurrency;

	private CurrencyDto sellSideBrokerageCurrency;

	private CurrencyDto buySideBrokerageBaseCurrency;

	private CurrencyDto sellSideBrokerageBaseCurrency;

	private BigDecimal buySideBaseAmount;

	private BigDecimal sellSideBaseAmount;

	private BigDecimal buySideConversionRate;

	private BigDecimal sellSideConversionRate;

	@JsonProperty(value = "lastTransactionAt")
	private Instant createdAt;

}
