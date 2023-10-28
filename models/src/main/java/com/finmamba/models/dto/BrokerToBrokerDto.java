package com.finmamba.models.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerToBrokerDto implements Serializable{

	//private TransactionDto transactionId;
	
	private CounterPartyTransactionFetchingDto cpTransactionId;
	
	private DirectionDto directionId;
	
	private Long statusId;
	
	private Instant lastTransactionAt;
	
}
