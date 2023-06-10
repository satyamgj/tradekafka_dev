package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SellSideConfirmationDto implements Serializable{

	private static final long serialVersionUID = 1L;

	Long id;

	InstitutionalEntityDto brokerId;
	
	InstitutionalEntityDto sellSideId;
	
	String transactionId;
	
	CounterpartyTransactionDto sellSideCounterpartyTransactionId;
	
}
