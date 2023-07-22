package com.finmamba.nonflow.services;

import java.util.List;

import com.finmamba.models.reports.NonFlowReportDto;
import com.finmamba.models.reports.SaveNonFlowDto;
import com.finmamba.models.reports.SearchNonFlowDto;
import com.finmamba.nonflow.exception.CustomNonFlowException;

public interface NonFlowOperationsServiceDal {


	public List<SaveNonFlowDto> saveNonFlowRequest(SaveNonFlowDto saveNonFlowDto) throws CustomNonFlowException;

	public List<NonFlowReportDto> getNonFlowRequest(SearchNonFlowDto nonFlowRequest) throws CustomNonFlowException;
	
}
