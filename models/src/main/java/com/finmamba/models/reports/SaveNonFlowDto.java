package com.finmamba.models.reports;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.lang.NonNull;

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
public class SaveNonFlowDto {
	
	@NonNull
	private Integer productId;
	@NonNull
	private Integer entityId;
	@NonNull
	private Integer buySideId;
	@NonNull
	private Integer sellSideId;
	@NonNull
	private Integer bankBuyCcyId;
	@NonNull
	private Integer bankSellCcyId;
	private Integer buySideBrokerageCcy;
	private Integer sellSideBrokerageCcy;
	private Integer buySideBrokerageBaseCcy;
	private Integer sellSideBrokerageBaseCcy;
	private BigDecimal buySideSpread;
	private BigDecimal sellSideSpread;
	private BigDecimal bankBuyAmount;
	private BigDecimal bankSellAmount;
	private BigDecimal buySideBaseAmount;
	private BigDecimal sellSideBaseAmount;
	private BigDecimal calculatedBuyCommission;
	private BigDecimal calculatedSellCommission;
	@NonNull
	private BigDecimal clientOutright;
	private BigDecimal buySideConversionRate;
	private BigDecimal sellSideConversionRate;
	private Boolean brokerCommissionModeIspip;
	@NonNull
	private Integer direction;
    private Instant createdAt;
    @NonNull
    private Integer createdBy;
    
    private int httpStatus;
    
}
