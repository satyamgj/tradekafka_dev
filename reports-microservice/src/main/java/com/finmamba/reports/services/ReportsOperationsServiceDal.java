package com.finmamba.reports.services;

import java.util.List;

import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.reports.CommissionReport;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.reports.exception.ReportNotFound;

public interface ReportsOperationsServiceDal {

	public List<SuperInstitutionalEntityDto> getTradeReport(TradeReport tradeReport) throws ReportNotFound;

	public List<SuperInstitutionalEntityDto> getCommissionReport(CommissionReport commissionReport) throws ReportNotFound;
	
	//public List<SavedNonFlowDto> getNonFlowReport(NonFlowRequest nonFlowReport) throws ReportNotFound;
	
}
