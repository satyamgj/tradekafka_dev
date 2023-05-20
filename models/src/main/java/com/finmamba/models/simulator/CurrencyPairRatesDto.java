package com.finmamba.models.simulator;

import java.io.Serializable;

import com.finmamba.models.dto.CurrencyDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CurrencyPairRatesDto implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private CurrencyDto buyCurrency;
	private CurrencyDto sellCurrency;
	private String ccyPair;
	private double bidPrice;
	private double midPrice;
	private double askPrice;
	private double lowPrice;
	private double highPrice;
	private double bidChange;
	private double bidPercentChange;
	private double askChange;
	private double askPercentChange;
	
	
	
}
