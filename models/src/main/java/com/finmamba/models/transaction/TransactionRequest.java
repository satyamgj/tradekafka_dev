package com.finmamba.models.transaction;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long product;

	private Long buySellDirection;

	private Long baseCurrency;

	private Long quoteCurrency;

	private BigDecimal buyAmount;

	private BigDecimal sellAmount;

	private BigDecimal sourceOutright;

	private BigDecimal spreadOutright;
	
	private BigDecimal destinationOutright;
	
	private BigDecimal buySideSpread;
	
	private BigDecimal sellSideSpread;
	
	private BigDecimal calculatedBuyCommission;
	
	private BigDecimal calculatedSellCommission;

	private Long tenorUnit;

	private Long source;
	
	private Long destination;

	private Boolean isRequestActive;
	
	private Boolean isPartial;

	private String transactionId;
	
	private Long createdBy;
	
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

	private Long responseInitiatedBy;
	
	private Long requestInitiatedBy;
}
