package com.finmamba.nonflow.services;

import java.util.List;

import com.finmamba.entities.transaction.NonFlow;
import com.finmamba.models.dto.NonFlowDto;
import com.finmamba.models.dto.SavedNonFlowDto;
import com.finmamba.nonflow.NonFlowRequest;
import com.finmamba.nonflow.exception.CustomNonFlowException;

public interface NonFlowOperationsServiceDal {


	public List<SavedNonFlowDto> saveNonFlowRequest(NonFlowDto nonFlowDto) throws CustomNonFlowException;

	public List<NonFlow> getNonFlowRequest(NonFlowRequest nonFlowRequest) throws CustomNonFlowException;
	
}
