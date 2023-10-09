package com.finmamba.models.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerOrderDto implements Serializable{

	private TransactionDto transactionId;
	
	private DirectionDto directionId;
	
	private Instant lastTransactionAt;
	
}
