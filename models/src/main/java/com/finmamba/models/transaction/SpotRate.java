package com.finmamba.models.transaction;

import java.math.BigDecimal;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class SpotRate {
	
	@NonNull
	private BigDecimal sourceSpotRate;
	
	@NonNull
	private BigDecimal sourceSpotSpread;
	
	@NonNull
	private BigDecimal destinationSpotRate;
	
}
