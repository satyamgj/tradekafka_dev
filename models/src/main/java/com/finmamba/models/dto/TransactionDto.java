package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class TransactionDto implements Serializable{


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

	private boolean isRequestActive;

	private String transactionId;

	private CustomerDto customer;
	
	private int httpStatus;
	
	private UserDto createdBy;
}
