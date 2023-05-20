package com.finmamba.models.transaction;

import java.math.BigDecimal;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class Outright {
	
	@NonNull
	private BigDecimal sourceOutright;
	
	@NonNull
	private BigDecimal spreadOutright;
	
	@NonNull
	private BigDecimal destinationOutright;
	
}
