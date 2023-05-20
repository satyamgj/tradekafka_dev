package com.finmamba.entities.reports;

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
@Table(name = "kotakworld", schema = "transaction")
@Builder
public class KotakTradeReport implements Serializable {

	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "direction_id")
	private int directionBuySell;

	@Column(name = "notional")
	private double notional;

	@Column(name = "bank_buy_ccy_id")
	private int bankBuyCcyId;

	@Column(name = "bank_sell_ccy_id")
	private int bankSellCcyId;
	
	@Column(name = "confirmation_initiated_by")
	private int counterParty;
	
	@Column(name = "client_outright")
	private double rate;

	@Column(name = "product_id")
	private int productId;

}
