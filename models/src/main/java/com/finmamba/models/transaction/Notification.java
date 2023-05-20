package com.finmamba.models.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
	
	private Long userId;
	
	private Long realmId;
	
	private Long transactionId;
	
	private Long sourceId;
	
	private Long destinationId;
	
	private Long actionId;
	
	private String notificationMessage;
	
	private String daddyTransactionId;
	
}
