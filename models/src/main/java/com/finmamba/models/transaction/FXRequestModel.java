package com.finmamba.models.transaction;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class FXRequestModel {

	// Will be used for RMID among other user roles
	// FE to provide value of key "sub" obtained in the access token.
	private String userId;
	
	
	private long institutionalEntityId;
	
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
	SpotRate spotRate;

	
	@NonNull
	SwapPoint swapPoint;
	
	
	@NonNull
	Outright outright;
	
	
	private int tenorValue;

	@NonNull
	private Long tenorUnitId;
	

	@NonNull
	private Long entityId;

}
