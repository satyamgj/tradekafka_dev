package com.finmamba.models.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConfirmedOrderDto implements Serializable {
	private static final long serialVersionUID = 1L;

	//private Long id;

	private InstitutionalEntityDto brokerId;

	private String transactionId;

	private InstitutionalEntityDto buySideId;

	private InstitutionalEntityDto sellSideId;

	private CounterpartyTransactionDto buySideCounterpartyTransactionId;

	private CounterpartyTransactionDto sellSideCounterpartyTransactionId;

	private UserDto createdBy;

	private Instant createdAt;
	
	private Long total_count;

}
