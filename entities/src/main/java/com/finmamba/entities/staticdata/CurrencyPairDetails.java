package com.finmamba.entities.staticdata;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "rates", schema = "static")
@Data
public class CurrencyPairDetails implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@OneToOne
	private Currency baseCurrency;
	
	@OneToOne
	private Currency quoteCurrency;
	
	@Column(name = "bid_rate", insertable = false, updatable = false, precision = 4)
	private BigDecimal bidRate;
	
	@Column(name = "ask_rate", insertable = false, updatable = false, precision = 4)
	private BigDecimal askRate;
}
