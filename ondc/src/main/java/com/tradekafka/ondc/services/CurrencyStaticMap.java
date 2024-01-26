package com.tradekafka.ondc.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.Currency;

@Service
public class CurrencyStaticMap {

	Map<Long, Currency>currency = new HashMap<>();
	
	public Map<Long, Currency> getCurrencyMap(){
		return currency;
	}
	
	public void addCurrency(Currency ccy) {
		currency.put(ccy.getId(), ccy);
	}
}
