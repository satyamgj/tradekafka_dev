package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(
		schema = "transaction",
		name = "counterparty_transaction_details"
		)
@Builder
public class CounterpartyTransactionDetails implements Serializable {
private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "transaction_id", nullable = false)
	private String transactionId;
	
	@OneToOne
	@JoinColumn(name = "source_id", nullable = false)
	private InstitutionalEntity sourceId;
	
	@OneToOne
	@JoinColumn(name = "destination_id", nullable = false)
	private InstitutionalEntity destinationId;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "event_type_id", nullable = false)
	private EventType eventType;
	
	@Column(name = "request_is_active", nullable = true)
	private boolean isRequestActive;
	
	@Column(name="buy_side_spread")
	private BigDecimal buySideSpread;
	
	@Column(name="sell_side_spread")
	private BigDecimal sellSideSpread;
	
	@Column(name = "broker_commission_mode_ispip")
	private Boolean brokerCommissionIsPip; //True if value is adjusted in pips/False if absolute
	
	@OneToOne
	@JoinColumn(name = "buy_side_brokerage_ccy", nullable = false)
	private Currency buySideBrokerageCurrency;
	
	@OneToOne
	@JoinColumn(name = "sell_side_brokerage_ccy", nullable = false)
	private Currency sellSideBrokerageCurrency;
	
	@OneToOne
	@JoinColumn(name = "sell_side_brokerage_base_ccy", nullable = false)
	private Currency sellSideBrokerageBaseCurrency;
	
	@OneToOne
	@JoinColumn(name = "buy_side_brokerage_base_ccy", nullable = false)
	private Currency buySideBrokerageBaseCurrency;
	
	@Column(name = "buy_side_base_amount")
	private BigDecimal buySideBaseAmount;
	
	@Column(name = "sell_side_base_amount")
	private BigDecimal sellSideBaseAmount;
	
	@Column(name = "buy_side_conversion_rate")
	private BigDecimal buySideConversionRate;
	
	@Column(name = "sell_side_conversion_rate")
	private BigDecimal sellSideConversionRate;
	
	@Column(name = "calculated_buy_commission")
	private BigDecimal calculatedBuyCommission;
	
	@Column(name = "calculated_sell_commission")
	private BigDecimal calculatedSellCommission;
	
	@Column(name = "bank_buy_amount")
	private BigDecimal bankBuyAmount;
	
	@Column(name = "bank_sell_amount")
	private BigDecimal bankSellAmount;
	
	@Column(name= "client_outright")
	private BigDecimal clientOutright;
	
	@Column(name="is_partial")
	private Boolean isPartial;
	
	@Column(name="response_initiated_by")
	private InstitutionalEntity responseInitiatedBy;
	
	@Column(name="request_initiated_by")
	private InstitutionalEntity requestInitiatedBy;
	
	@OneToOne
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;
	
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	
	
	
	
	
	
	
	
	
	
}
