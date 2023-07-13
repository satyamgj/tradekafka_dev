package com.finmamba.nonflow.services;

import java.util.List;

import com.finmamba.models.dto.NonFlowDto;
import com.finmamba.models.dto.SavedNonFlowDto;

public interface NonFlowOperationsService {
	/*
	 * Services to process requests and responses
	 */
	public List<SavedNonFlowDto> saveNonFlowTrade(NonFlowDto nonFlowModel);

	//public List<NonFlow> getNonFlowTrades(NonFlowRequest nonFlowRequest);

	
}
