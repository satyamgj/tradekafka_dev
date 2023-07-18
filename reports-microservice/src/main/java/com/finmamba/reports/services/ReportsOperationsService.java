package com.finmamba.reports.services;

import java.util.List;

import com.finmamba.models.dto.ReportsDto;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.reports.exception.ReportNotFound;

public interface ReportsOperationsService {
	/*
	 * Services to process requests and responses
	 */
	public List<ReportsDto> processTradeReportRequest(TradeReport tradeReport) throws ReportNotFound;


}
