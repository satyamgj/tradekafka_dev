package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;

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
@Builder
public class QuoteRequestDto implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private ProductDto product;

	private EventTypeDto eventType;

	private DirectionDto buySellDirection;

	private CurrencyDto baseCurrency;

	private CurrencyDto quoteCurrency;

	private BigDecimal buyAmount;

	private BigDecimal sellAmount;

	private BigDecimal sourceOutright;
	
	private BigDecimal spreadOutright;
	
	private BigDecimal destinationOutright;

	private TenorDto tenorUnit;

	private InstitutionalEntityDto source;

	private Boolean isRequestActive;

	private String transactionId;

	private CustomerDto customer;
	
	private Boolean isPartial;
	
	private int httpStatus;
	
	private UserDto createdBy;
//	private Instant createdAt;
	

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
