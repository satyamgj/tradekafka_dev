package com.finmamba.models.transaction;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest implements Serializable{

	private static final long serialVersionUID = 1L;


	private BigDecimal buyAmount;

	private BigDecimal sellAmount;

	private BigDecimal sourceOutright;
	
	private BigDecimal destinationOutright;
	
	private BigDecimal buySideSpread;
	
	private BigDecimal sellSideSpread;
	
	private BigDecimal calculatedBuyCommission;
	
	private BigDecimal calculatedSellCommission;

	private Long source;
	
	private Long destination;
	
	private Long transactionId;
	
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
	
	private Long requestInitiatedBy; //Transaction
	
	private Long legInitiatedBy; //counterpartyTransactionDetails
	
	private Long direction;
	
	private Boolean sendToBrokers; //True if broadcast to all brokers : False if broadcast to all banks.
}
