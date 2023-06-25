package com.finmamba.models.transaction;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmRequest implements Serializable{

	private static final long serialVersionUID = 1L;

	Long brokerId;
		
	Long buySideCounterParty;
	
	Long sellSideCounterPart;
	
	Long buySideCounterPartyTransactionId;
	
	Long sellSideCounterPartyTransactionId;
	
	Long createdBy; 

	
}
