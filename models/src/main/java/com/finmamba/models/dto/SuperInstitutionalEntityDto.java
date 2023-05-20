package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SuperInstitutionalEntityDto implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private Long total_count;
	private ProductDto product;
	private DirectionDto buySellDirection;
	private CurrencyDto baseCurrency;
	private CurrencyDto quoteCurrency;
	private BigDecimal buyAmount;
	private BigDecimal sellAmount;
	private BigDecimal sourceSpotRate;
	private BigDecimal sourceSpotSpread;
	private BigDecimal destinationSpotRate;
	private BigDecimal sourceSwapPoints;
	private BigDecimal sourceSwapSpread;
	private BigDecimal destinationSwapRate;
	private BigDecimal sourceOutright;
	private BigDecimal spreadOutright;
	private BigDecimal destinationOutright;
	private TenorDto tenorUnit;
	private BigDecimal notional;
	private InstitutionalEntityDto source;
	private boolean isRequestActive;
	private String transactionId;
	private EventTypeDto eventType;
	private CustomerDto customer;
	private InstitutionalEntityDto destinationId;
	private InstitutionalEntityDto negotiationInitiatedBy;
	private InstitutionalEntityDto confirmationInitiatedBy;
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

//	private Instant createdAt;

}
