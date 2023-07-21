package com.finmamba.nonflow.services;

import java.util.List;

import com.finmamba.entities.transaction.NonFlow;
import com.finmamba.models.reports.SaveNonFlowDto;
import com.finmamba.models.reports.SearchNonFlowDto;

public interface NonFlowOperationsService {
	/*
	 * Services to process requests and responses
	 */
	public List<SaveNonFlowDto> saveNonFlowTrade(SaveNonFlowDto nonFlowModel);

	public List<SaveNonFlowDto> getNonFlowTrades(SearchNonFlowDto nonFlowRequest);

	
}
