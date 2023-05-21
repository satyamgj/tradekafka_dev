package com.finmamba.entities.configuration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.InstitutionalEntity;

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
@Table(name = "entityconfigsettings", schema = "configuration")
@Builder
public class EntityConfigurationSettings implements Serializable {

	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@OneToOne
	@JoinColumn(name = "entity_id", referencedColumnName = "id")
	private InstitutionalEntity entityId;

	@Column(name = "default_spread_buy")
	private BigDecimal defaultSpreadBuy;
	
	@Column(name = "default_spread_sell")
	private BigDecimal defaultSpreadSell;

	@Column(name="stp")
	private Boolean stp;
	
	@Column(name="is_pip_or_absolute")
	private Boolean isPipOrAbsolute;
	
	@Column(name="point_shift_decimal")
	private int pointShiftDecimal;
	
	@OneToOne
	@JoinColumn(name="buy_commission_ccy", referencedColumnName = "id")
	private Currency buyCommissionCurrency;
	
	@OneToOne
	@JoinColumn(name="sell_commission_ccy", referencedColumnName = "id")
	private Currency sellCommissionCurrency;
	
	@Column(name="buy_side_absolute_commission")
	private BigDecimal buySideAbsoluteCommission;
	
	@Column(name="sell_side_absolute_commission")
	private BigDecimal sellSideAbsoluteCommission;
	
	@Column(name="buy_side_base_commission_value")
	private BigDecimal buySideBaseCommissionValue;
	
	@Column(name="sell_side_base_commission_value")
	private BigDecimal sellSideBaseCommissionValue;
	
	@OneToOne
	@JoinColumn(name="buy_side_base_commission_ccy", referencedColumnName = "id")
	private Currency buySideBaseCommissionCurrency;
	
	@OneToOne
	@JoinColumn(name="sell_side_base_commission_ccy", referencedColumnName = "id")
	private Currency sellSideBaseCommissionCurrency;
	
	@Column(name="created_at", insertable = false)
	private Timestamp createdAt;
	
	
}
