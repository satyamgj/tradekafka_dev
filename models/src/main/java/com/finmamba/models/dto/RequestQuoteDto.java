package com.finmamba.models.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RequestQuoteDto {
	private String product;

	private String buySellDirection;

	private String currencyPair;

	private int tenorValue;

	private BigDecimal notional;

	private Timestamp quoteValidityCalculated;

}
