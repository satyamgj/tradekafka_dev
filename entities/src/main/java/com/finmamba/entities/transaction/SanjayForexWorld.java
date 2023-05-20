package com.finmamba.entities.transaction;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sanjayforexworld", schema = "transaction")
@SuperBuilder
public class SanjayForexWorld  extends SuperInstitutionalEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name = "buy_side_spread", nullable = true)
	private BigDecimal buySideSpread;
	@Column(name = "sell_side_spread", nullable = true)
	private BigDecimal sellSideSpread;
	@Column(name = "calculated_buy_commission", nullable = true)
	private BigDecimal calculatedBuyCommission;
	@Column(name = "calculated_sell_commission", nullable = true)
	private BigDecimal calculatedSellCommission;
}
