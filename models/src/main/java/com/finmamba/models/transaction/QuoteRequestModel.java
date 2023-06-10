package com.finmamba.models.transaction;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Data;

@Data
public class QuoteRequestModel implements Serializable{

	// Will be used for RMID among other user roles
	// FE to provide value of key "sub" obtained in the access token.
	private String userId;
	
	
	private Long institutionalEntityId;
	
	// Non Null because the RM may place OBO the customer
	private Long customerId;

	// productId - SPOT, FWD
	@NonNull
	private Long productId;

	// Maps to bankBuyCCyID
	@NonNull
	private Long baseCurrencyId;

	// Maps to bankSellCCyID
	@NonNull
	private Long quoteCurrencyId;


	@NonNull
	private Long buySellDirectionId;
	
	
	@Nullable
	private BigDecimal buyAmount;
	

	@Nullable
	private BigDecimal sellAmount;

	@NonNull
	BigDecimal sourceOutright;

	
	@NonNull
	BigDecimal spreadOutright;
	
	
	@NonNull
	BigDecimal destinationOutright;
	
	
	private int tenorValue;

	@NonNull
	private Long tenorUnitId;
	
	@NonNull
	private Boolean isPartial;
	

}
