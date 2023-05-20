package com.finmamba.models.transaction;

import org.springframework.lang.NonNull;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceDestination {
	
	@NonNull
	private long sourceEntityId;
	
	@NonNull
	private long destinationEntityId;
	
	@NonNull
	private long prevSourceEntityId;
	
}
