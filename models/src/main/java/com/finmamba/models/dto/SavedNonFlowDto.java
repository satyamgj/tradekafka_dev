package com.finmamba.models.dto;

import java.math.BigDecimal;
import java.sql.Date;


import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonFormat;

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
public class SavedNonFlowDto {

	private Integer userId;
	
	private Long total_count;

	private InstitutionalEntityDto institutionalId;
	
	private InstitutionalEntityDto institutionalSourceEntityId;

	// Added for ease of access. To be fetched from token in the FE
	private String institutionalSourceEntityName;

	private InstitutionalEntityDto institutionalDestinationEntityId;

	// Added for ease of access. To be fetched from token in the FE
	private String institutionalDestinationEntityName;
	
	// Non Null because the RM may place OBO the customer
	private CustomerDto customerId;

	// productId - SPOT, FWD
	@NonNull
	private ProductDto productId;

	// Maps to bankBuyCCyID
	@NonNull
	private CurrencyDto baseCurrencyId;

	// Maps to bankSellCCyID
	@NonNull
	private CurrencyDto quoteCurrency;

	@NonNull
	private DirectionDto buySellDirection;

	@Nullable
	private BigDecimal buyAmount;

	@Nullable
	private BigDecimal sellAmount;

	@NonNull
	private BigDecimal spotRate;

	private BigDecimal outright;

	private Long tenorValue;

	@NonNull
	private TenorDto tenorUnitId;

	@NonNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date uiRequestTimeStamp;


//	@NonNull
	private CustomerSegmentDto customerSegmentId;

	private int httpStatus;
	
	@NonNull
	private boolean isPipOrAbsolute;
	
	private BigDecimal buySideAbsoluteCommision;
	
	private BigDecimal sellSideAbsoluteCommision;
	
	private CurrencyDto perBaseBuyCurrency;
	
	private CurrencyDto perBaseSellCurrency;
	
	private CurrencyDto buySideCurrencyId;
	
	private CurrencyDto sellSideCurrencyId;
	
	private BigDecimal perBaseBuyCurrencyAmount;
	
	private BigDecimal perBaseSellCurrencyAmount;
	
	private BigDecimal buySideCommisionRate;
	
	private BigDecimal sellSideCommisionRate;

}