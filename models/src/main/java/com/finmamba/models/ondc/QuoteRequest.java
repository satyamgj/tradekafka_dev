package com.finmamba.models.ondc;

import java.io.Serializable;
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
public class QuoteRequest implements Serializable{

	private static final long serialVersionUID = 1L;
	Long userId;
	Long directionId;
	Long baseCurrencyId;
	Long quoteCurrencyId;
	BigDecimal amount;
	String responseYN;
	BigDecimal askingRate;
	Long responseTo;
	
}
