package com.finmamba.models.transaction;

import java.math.BigDecimal;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class SwapPoint {
	
	@NonNull
	private BigDecimal sourceSwapPoints;
	
	@NonNull
	private BigDecimal sourceSwapSpread;
	
	@NonNull
	private BigDecimal destinationSwapRate;
	
}
