package com.finmamba.entities.staticdata;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
//@Table(name = "currencyspotrates", schema = "configuration")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyPairRates implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private long id;
	
	@Column(name = "buy_currency")
	private String baseCurrency;
	
	@Column(name = "buy_currency_id")
	private Long buyCurrencyId;
	
	@Column(name = "sell_currency")
	private String quoteCurrency;
	
	@Column(name = "sell_currency_id")
	private Long sellCurrencyId;
	
	@Column(name = "bid_price")	
	private BigDecimal bidPrice;
	
	@Column(name = "mid_price")
	private BigDecimal midPrice;
	
	@Column(name = "ask_price")
	private BigDecimal askPrice;
	
	@Column(name = "low_price")
	private BigDecimal lowPrice;
	
	@Column(name = "high_price")
	private BigDecimal highPrice;
	
//	@Column(name = "close_price")
//	private BigDecimal closePrice;
//	
//	@Column(name = "change")
//	private BigDecimal change;
//	
//	@Column(name = "change_percent")
//	private BigDecimal changePercent;
	
}
