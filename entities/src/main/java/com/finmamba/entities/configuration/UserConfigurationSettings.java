package com.finmamba.entities.configuration;

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
@Table(name = "userconfigsettings", schema = "configuration")
@Builder
public class UserConfigurationSettings implements Serializable {

	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id")
	private long userId;

	@Column(name = "setting_default_spread_buy")
	private double settingDefaultSpreadBuy;

	@Column(name = "setting_default_spread_sell")
	private double settingDefaultSpreadSell;

	@Column(name = "setting_stp")
	private boolean settingStp;

	@Column(name = "setting_buy_currency")
	private int settingBuyCurrency;

	@Column(name = "setting_sell_currency")
	private int settingSellCurrency;

	@Column(name = "setting_spread_unit")
	private int settingSpreadUnit;

	@Column(name = "setting_max_notional")
	private double settingMaxNotional;

	@Column(name = "setting_point_shift_decimal")
	private int settingPointShiftDecimal;
	
	@Column(name = "setting_preferred_currency")
	private int settingPreferredCurrency;
	
	@Column(name = "setting_absolute_commission_amount")
	private double settingAbsoluteCommissionAmount;
}
