package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class buySideConfirmDto implements Serializable{
	
	private static final long serialVersionUID = 1L;

	Long id;

	InstitutionalEntityDto brokerId;
	
	InstitutionalEntityDto buySideId;
	
	String transactionId;
	
	CounterpartyTransactionDto buySideCounterpartyTransactionId;
	
}
