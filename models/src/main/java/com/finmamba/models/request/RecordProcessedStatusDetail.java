package com.finmamba.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordProcessedStatusDetail {
	private long recordId;
	private Boolean processedStatus;
}
