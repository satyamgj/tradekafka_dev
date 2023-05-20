package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.CurrencyPairDetails;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.RecordStatus;
import com.finmamba.entities.staticdata.RequestType;
import com.finmamba.entities.staticdata.Tenor;

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
@Table(name = "quote_request", schema = "transaction")
@Builder
public class RequestQuote implements Serializable, Cloneable {
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
	private Long id;

	// Will be used for RMID among other user roles

	@OneToOne
	@JoinColumn(name = "users_id", nullable = false)
	private User user;

	// Non Null because the RM may place OBO the cusstomer

	@OneToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	// productId - SPOT, FWD
	@OneToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	// requestTypeId - Negotiation or Initial Request
	@OneToOne
	@JoinColumn(name = "request_type_id", nullable = false)
	private RequestType requestType;

	// parentId - Initial Request defaults to -1
	@Column(name = "parent_id", nullable = true, updatable = false)
	private long parentId;

	@OneToOne
	@JoinColumn(name = "direction_id", nullable = false)
	private Direction buySellDirection;

	@OneToOne
	@JoinColumn(name = "currency_pair_id", nullable = false)
	private CurrencyPairDetails currencyPairDetails;

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

	@Column(name = "tenor_value", nullable = false)
	private int tenorValue;

	@OneToOne
	@JoinColumn(name = "tenor_unit_id", nullable = false)
	private Tenor tenorUnit;

	@Column(name = "notional", nullable = false)
	private BigDecimal notional;

	@OneToOne
	@JoinColumn(name = "source_id", nullable = false)
	private InstitutionalEntity source;

	@OneToOne
	@JoinColumn(name = "destination_id", nullable = false)
	private InstitutionalEntity destination;

	@JsonIgnore
	@Column(name = "fe_request_timestamp", nullable = false)
	private Timestamp uiRequestTimeStamp;

	@JsonIgnore
	@Column(name = "db_request_timestamp", nullable = false)
	private Timestamp dbRequestTimeStamp;

	@JsonIgnore
	@Column(name = "utc_request_timestamp", nullable = false)
	private Timestamp utcRequestTimeStamp;

	@JsonIgnore
	@Column(name = "quote_validity_received", nullable = false)
	private Timestamp quoteValidityReceived;

	@JsonIgnore
	@Column(name = "quote_validity_calculated", nullable = false)
	private Timestamp quoteValidityCalculated;

	@Column(name = "time_to_live", nullable = false)
	private int timeToLive;

	@Column(name = "request_is_active", nullable = true)
	private boolean isRequestActive;
	
	@Column(name = "transaction_id", nullable = false)
	private String transactionId;
	
	@Column(name = "reason_for_expiry", nullable = true)
	private String reasonForExpiry;
	
	@Column(name = "record_is_processed", nullable = true)
	private boolean isRecordProcessed;
	
	@OneToOne
	@JoinColumn(name = "record_status", nullable = false)
	private RecordStatus recordStatus;
	
	
	@Column(name = "view_expanded", nullable = true)
	private boolean viewExpanded;
	
	

	@PostLoad
	private void postLoad() {
		if (this.quoteValidityCalculated != null) {
			this.timeToLive = (int) LocalDateTime.now().until(this.quoteValidityCalculated.toLocalDateTime(),
					ChronoUnit.SECONDS);
		}

	}

//	@Column
//	private EntityDetail entity;
//
//	@Column
//	private Date tradeDate;
//
//	@Column
//	private CustomerSegment customerSegment;
//
//	@Column
//	private Date valueDate;

}
