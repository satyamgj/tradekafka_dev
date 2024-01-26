package com.finmamba.models.ondc;

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
public class OrderForMatchEngine implements Serializable{
	private static final long serialVersionUID = 1L;
	Long quoteRequestId;
	String baseCurrency;
	String quoteCurrency;
	BigDecimal askingRate;
	BigDecimal amount;
	Long directionId;
	Long initiatorId;
}
