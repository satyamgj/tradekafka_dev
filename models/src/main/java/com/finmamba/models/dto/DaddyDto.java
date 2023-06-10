package com.finmamba.models.dto;

import java.math.BigDecimal;

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
public class DaddyDto implements Cloneable {

	private Long id;

	private ProductDto product;

	private EventTypeDto eventType;

	private DirectionDto buySellDirection;

	private CurrencyDto baseCurrency;

	private CurrencyDto quoteCurrency;

	private BigDecimal buyAmount;

	private BigDecimal sellAmount;

	private BigDecimal sourceOutright;

	private BigDecimal spreadOutright;
	
	private BigDecimal destinationOutright;
	
	private BigDecimal buySideSpread;
	
	private BigDecimal sellSideSpread;
	
	private BigDecimal calculatedBuyCommission;
	
	private BigDecimal calculatedSellCommission;

	private TenorDto tenorUnit;

	private BigDecimal notional;

	private InstitutionalEntityDto source;

	private Boolean isRequestActive;

	private String transactionId;

	private CustomerDto customer;
	
	private int httpStatus;
	
	private InstitutionalEntityDto negotiationInitiatedBy;
	private InstitutionalEntityDto confirmationInitiatedBy;
	
	private UserDto createdBy;
//	private Instant createdAt;
	
	private Boolean brokerCommissionIsPip;
	
	private BigDecimal brokerCommission;
	
	private Long buySideBrokerageCurrency;
	
	private Long sellSideBrokerageCurrency;
	
	private Long buySideBrokerageBaseCurrency;
	
	private Long sellSideBrokerageBaseCurrency;
	
	private BigDecimal buySideBaseAmount;
	
	private BigDecimal sellSideBaseAmount;
	
	private BigDecimal buySideConversionRate;
	
	private BigDecimal sellSideConversionRate;

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
