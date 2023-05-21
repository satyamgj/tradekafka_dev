package com.finmamba.reports.services;

import java.util.List;

import com.finmamba.models.dto.SavedNonFlowDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.reports.CommissionReport;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.nonflow.NonFlowRequest;
import com.finmamba.reports.exception.ReportNotFound;

public interface ReportsOperationsService {
	/*
	 * Services to process requests and responses
	 */
	public List<SuperInstitutionalEntityDto> processTradeReportRequest(TradeReport tradeReport) throws ReportNotFound;

	public List<SuperInstitutionalEntityDto> processCommissionReportRequest(CommissionReport commissionReport) throws ReportNotFound;

	public List<SavedNonFlowDto> processNonFlowReportRequest(NonFlowRequest nonFlowReport) throws ReportNotFound;

}
