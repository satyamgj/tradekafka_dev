package com.finmamba.entities.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.CustomerSegment;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
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
@Table(name = "nonflow", schema = "transaction")
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

	@OneToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;
	
	// productId - SPOT, FWD
	@OneToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
	
	@Column(name = "user_id", nullable = false)
	private Integer userId ;
	
	@OneToOne
	@JoinColumn(name = "institutional_id", nullable = false)
	private InstitutionalEntity institutionalId;
	
	@OneToOne
	@JoinColumn(name = "institutional_source_entity_id", nullable = false)
	private InstitutionalEntity institutionalSourcEntityId;
	
	@Column(name = "institutional_source_entity_name", nullable = false)
	private String institutionalSourceEntityName;
	
	@OneToOne
	@JoinColumn(name = "institutional_destination_entity_id", nullable = false)
	private InstitutionalEntity institutionalDestinationEntityId;

	@Column(name = "institutional_destination_entity_name", nullable = false)
	private String institutionalDestinationEntityName;

	
	@OneToOne
	@JoinColumn(name = "buy_sell_direction_id", nullable = false)
	private Direction buySellDirection;

	// Maps to bankBuyCCyID
	@OneToOne
	@JoinColumn(name = "base_currency_id", nullable = false)
	private Currency baseCurrency;
	
	// Maps to bankSellCCyID
	@OneToOne
	@JoinColumn(name = "quote_currency_id", nullable = false)
	private Currency quoteCurrency;

	// Buy Amount
	@Column(name = "buy_amount", nullable = true)
	private BigDecimal buyAmount;

	// Buy Amount
	@Column(name = "sell_amount", nullable = true)
	private BigDecimal sellAmount;

	// Matrix of 3X3 (9 entries) for spot, spread and outright

	// All Spot Values
	@Column(name = "spot_rate", nullable = true)
	private BigDecimal spotRate;
	
	@Column(name = "outright", nullable = true)
	private BigDecimal outright;
	
	@Column(name = "tenor_value", nullable = false)
	private Long tenorValue;

	
	@OneToOne
	@JoinColumn(name = "tenor_unit_id", nullable = false)
	private Tenor tenorUnit;

	@Column(name = "ui_request_timestamp", nullable = false)
	private Date uiRequestTimestamp;
	

	@Column(name = "db_request_timestamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP" ,nullable = false)
	private Date dbRequestTimestamp;
	
	@OneToOne
	@JoinColumn(name = "customer_segment_id", nullable = false)
	private CustomerSegment customerSegmentId;
	
	@Column(name = "is_pip_or_absolute", nullable =false)
	private boolean isPipOrAbsolute;
	
	@Column(name = "buy_side_absolute_commision", nullable =false)
	private BigDecimal buySideAbsoluteCommision;
	
	@Column(name = "sell_side_absolute_commision", nullable =false)
	private BigDecimal sellSideAbsoluteCommision;
	
	@OneToOne
	@JoinColumn(name = "per_base_buy_currency", nullable = false)
	private Currency perBaseBuyCurrency;
	
	@OneToOne
	@JoinColumn(name = "per_base_sell_currency", nullable = false)
	private Currency perBaseSellCurrency;
	
	@OneToOne
	@JoinColumn(name = "buy_side_currency_id", nullable = false)
	private Currency buySideCurrencyId;
	
	@OneToOne
	@JoinColumn(name = "sell_side_currency_id", nullable = false)
	private Currency sellSideCurrencyId;
	
	@Column(name = "per_base_buy_currency_amount", nullable =false)
	private BigDecimal perBaseBuyCurrencyAmount;
	
	@Column(name = "per_base_sell_currency_amount", nullable =false)
	private BigDecimal perBaseSellCurrencyAmount;
	
	@Column(name = "buy_side_commision_rate", nullable =false)
	private BigDecimal buySideCommisionRate;
	
	@Column(name = "sell_side_commision_rate", nullable =false)
	private BigDecimal sellSideCommisionRate;
}
