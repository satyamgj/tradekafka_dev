package com.finmamba.entities.simulator;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Entity
@Table(name = "currencyspotrates", schema = "configuration")
@Builder
public class SimulatedSpotRates implements Serializable {

	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "buy_currency_id")
	private int buyCurrencyId;

	@Column(name = "sell_currency_id")
	private int sellCurrencyId;

	@Column(name = "bid_price")
	private double bidPrice;

	@Column(name = "ask_price")
	private double askPrice;

	@Column(name = "mid_price")
	private double midPrice;

}
