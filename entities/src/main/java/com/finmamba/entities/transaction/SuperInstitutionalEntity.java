package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.Tenor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SuperInstitutionalEntity implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	private Long total_count;
	
	// Will be used for RMID among other user roles

	// productId - SPOT, FWD
	@OneToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	// requestTypeId - Negotiation or Initial Request
//	@OneToOne
//	@JoinColumn(name = "event_type_id", nullable = false)
//	private RequestType eventType;

	@OneToOne
	@JoinColumn(name = "direction_id", nullable = false)
	private Direction buySellDirection;

	// Maps to bankBuyCCyID
	@OneToOne
	@JoinColumn(name = "bank_buy_ccy_id", nullable = false)
	private Currency baseCurrency;

	// Maps to bankSellCCyID
	@OneToOne
	@JoinColumn(name = "bank_sell_ccy_id", nullable = false)
	private Currency quoteCurrency;

	// Buy Amount
	@Column(name = "bank_buy_amount", nullable = true)
	private BigDecimal buyAmount;

	// Buy Amount
	@Column(name = "bank_sell_amount", nullable = true)
	private BigDecimal sellAmount;

	// Matrix of 3X3 (9 entries) for spot, spread and outright

	// All Spot Values
	@Column(name = "bank_spot_rate", nullable = true)
	private BigDecimal sourceSpotRate;
	@Column(name = "bank_spot_spread", nullable = true)
	private BigDecimal sourceSpotSpread;
	// ** clientSpotRate is calculated as addition of above 2**
	@Column(name = "client_spot_rate", nullable = true)
	private BigDecimal destinationSpotRate;

	// All Swap Values -- All values default to 0
	@Column(name = "bank_swap_points", nullable = true)
	private BigDecimal sourceSwapPoints;
	@Column(name = "bank_swap_spread", nullable = true)
	private BigDecimal sourceSwapSpread;
	// ** clientSwapRate is calculated as addition of above 2**
	@Column(name = "client_swap_points", nullable = true)
	private BigDecimal destinationSwapRate;

	// All outright values -- All values default to non-editable additions
	// ** bankOutright is calculated as addition of bankSpotRate and
	// bankSwapPoints**
	@Column(name = "bank_outright", nullable = true)
	private BigDecimal sourceOutright;
	// ** spreadOutright is calculated as addition of bankSpotSpread and
	// bankSwapSpread**
	@Column(name = "spread_outright", nullable = true)
	private BigDecimal spreadOutright;
	@Column(name = "client_outright", nullable = true)
	// ** clientOutright is calculated as addition of clientSpotRate and
	// clientSwapRate**
	private BigDecimal destinationOutright;

	@OneToOne
	@JoinColumn(name = "tenor_unit_id", nullable = false)
	private Tenor tenorUnit;

	@Column(name = "notional", nullable = false)
	private BigDecimal notional;

	@OneToOne
	@JoinColumn(name = "source_id", nullable = false)
	private InstitutionalEntity source;

	@Column(name = "request_is_active", nullable = true)
	private boolean isRequestActive;

	@Column(name = "transaction_id", nullable = false)
	private String transactionId;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "event_type_id", nullable = false)
	private EventType eventType;

	@OneToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@OneToOne
	@JoinColumn(name = "destination_id", nullable = false)
	private InstitutionalEntity destinationId;
	
	@OneToOne
	@JoinColumn(name = "negotiation_initiated_by", nullable = false)
	private InstitutionalEntity negotiationInitiatedBy;
	
//	@Column(name = "created_by", nullable = false)
	// To store id of user_entity table of keycloak
	// id of user_entity table of keycloak is also available in transaction.users table to store application specific user information.
	@OneToOne
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;
	
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	
	@OneToOne
	@JoinColumn(name = "confirmation_initiated_by", nullable = false)
	private InstitutionalEntity confirmationInitiatedBy;
	
	
	@Column(name = "broker_commission_mode_ispip")
	private Boolean brokerCommissionIsPip; //True if value is adjusted in pips/False if absolute
	
	@Column(name = "broker_commission")
	private BigDecimal brokerCommission; //True if value is adjusted in pips/False if absolute
	
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
	
	@Column(name="buy_side_spread")
	private BigDecimal buySideSpread;
	
	@Column(name="sell_side_spread")
	private BigDecimal sellSideSpread;
	
	
	
}
