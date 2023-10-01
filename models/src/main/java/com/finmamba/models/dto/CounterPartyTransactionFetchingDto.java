package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CounterPartyTransactionFetchingDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private TransactionDto transactionId; //While fetching its null

//	private ProductDto product;
	
	private DirectionDto directionId;
	
//	private CurrencyDto baseCurrency;
//	
//	private CurrencyDto quoteCurrency;
	
	private BigDecimal buyAmount;

	private BigDecimal sellAmount;
	
	private BigDecimal clientOutright;
	
//	private DirectionDto direction;

	private TenorDto tenorUnit;
	
	private InstitutionalEntityDto source;
	
	private InstitutionalEntityDto destinationId;
	
	private boolean isRequestActive;
	
	private EventTypeDto eventType;
		
//	private UserDto createdBy;
	
	@Nullable
	private BigDecimal buySideSpread;
	@Nullable
	private BigDecimal sellSideSpread;
	
	@Nullable
	private BigDecimal calculatedBuyCommission;
	
	@Nullable
	private BigDecimal calculatedSellCommission;
	
	@Nullable
	private Boolean brokerCommissionIsPip;
	
	@Nullable
	private BigDecimal brokerCommission;

	@Nullable
	private CurrencyDto buySideBrokerageCurrency;

	@Nullable
	private CurrencyDto sellSideBrokerageCurrency;

	@Nullable
	private CurrencyDto buySideBrokerageBaseCurrency;

	@Nullable
	private CurrencyDto sellSideBrokerageBaseCurrency;

	@Nullable
	private BigDecimal buySideBaseAmount;
	@Nullable
	private BigDecimal sellSideBaseAmount;
	@Nullable
	private BigDecimal buySideConversionRate;
	@Nullable
	private BigDecimal sellSideConversionRate;
	@Nullable
	private Instant createdAt;
}
