package com.finmamba.reports.services;

import java.util.List;

import com.finmamba.models.dto.ReportsDto;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.reports.exception.ReportNotFound;

public interface ReportsOperationsServiceDal {

	public List<ReportsDto> getTradeReport(TradeReport tradeReport) throws ReportNotFound;

}
