package com.finmamba.models.configuration;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class EntityConfigurationSettingDto implements Serializable {

	/**
	 * This will fetch the following values from the table
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	
	@NonNull
	private Long entityId;

	private BigDecimal settingDefaultSpreadBuy;

	private BigDecimal settingDefaultSpreadSell;

	private boolean settingStp;

	private int settingBuyCurrency;

	private int settingSellCurrency;

	private int settingSpreadUnit;

	private double settingMaxNotional;

	private int settingPointShiftDecimal;
	
	private int settingPreferredCurrency;
	
	private BigDecimal settingAbsoluteCommissionAmount;
}
