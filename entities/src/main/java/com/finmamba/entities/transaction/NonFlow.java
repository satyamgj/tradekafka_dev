package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
@Table(name = "nonflow_transaction_details", schema = "transaction")
@Builder
public class NonFlow implements Serializable, Cloneable {
	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	// Will be used for RMID among other user roles

	// productId - SPOT, FWD
	@Column(name = "product_id", nullable = false)
	private Integer product;

	@Column(name = "entity_id", nullable = false)
	private Integer entityId;

	@Column(name = "buy_side_id", nullable = false)
	private Integer buySideId;

	@Column(name = "sell_side_id", nullable = false)
	private Integer sellSideId;

	@Column(name = "buy_side_brokerage_ccy", nullable = true)
	private Integer buySideBrokerageCcy;

	@Column(name = "sell_side_brokerage_ccy", nullable = true)
	private Integer sellSideBrokerageCcy;

	@Column(name = "buy_side_brokerage_base_ccy", nullable = true)
	private Integer buySideBrokerageBaseCcy;

	@Column(name = "sell_side_brokerage_base_ccy", nullable = true)
	private Integer sellSideBrokerageBaseCcy;

	@Column(name = "buy_side_spread", nullable = true)
	private BigDecimal buySideSpread;

	@Column(name = "sell_side_spread", nullable = true)
	private BigDecimal sellSideSpread;

	@Column(name = "bank_buy_amount", nullable = true)
	private BigDecimal bankBuyAmount;

	@Column(name = "bank_sell_amount", nullable = true)
	private BigDecimal bankSellAmount;

	@Column(name = "buy_side_base_amount", nullable = true)
	private BigDecimal buySideBaseAmount;

	@Column(name = "sell_side_base_amount", nullable = true)
	private BigDecimal selSideBaseAmount;

	@Column(name = "calculated_buy_commission", nullable = true)
	private BigDecimal calculatedBuyCommission;

	@Column(name = "calculated_sell_commission", nullable = true)
	private BigDecimal calculatedSellCommission;

	@Column(name = "client_outright", nullable = false)
	private BigDecimal clientOutright;

	@Column(name = "buy_side_conversion_rate", nullable = true)
	private BigDecimal buySideConversionRate;

	@Column(name = "sell_side_conversion_rate", nullable = true)
	private BigDecimal sellSideConversionRate;

	@Column(name = "broker_commission_mode_ispip", nullable = false)
	private Boolean brokerCommissionModeIsPip;

	@Column(name = "direction", nullable = false)
	private Integer direction;

	@Column(name = "created_by", nullable = false)
	private Integer createdBy;

	@Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
	private Instant createdAt;

}