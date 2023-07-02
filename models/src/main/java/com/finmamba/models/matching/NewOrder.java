package com.finmamba.models.matching;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import com.finmamba.models.dto.CurrencyDto;
import com.finmamba.models.dto.DirectionDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewOrder implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Long orderId;
	private CurrencyDto buyCurrency;
	private CurrencyDto sellCurrency;
	private Boolean isPartial;
	private DirectionDto direction;
	private BigDecimal askingRate;
	private BigDecimal buyAmount;
	private BigDecimal sellAmount;
	private Timestamp insertionTime;

}
