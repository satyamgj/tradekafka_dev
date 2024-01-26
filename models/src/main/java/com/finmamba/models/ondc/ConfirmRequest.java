package com.finmamba.models.ondc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmRequest {
	private Long buyTrxnId;
	private Long sellTrxnId;
}
