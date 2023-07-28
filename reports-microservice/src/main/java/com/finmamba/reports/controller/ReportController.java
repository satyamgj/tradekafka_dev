package com.finmamba.reports.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.models.dto.ReportsDto;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.report.util.TradeReportExcelExporter;
import com.finmamba.reports.exception.ReportNotFound;
import com.finmamba.reports.services.ReportsOperationsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
@Slf4j
public class ReportController {
	// These are reports that are generated from the trades that have been traded on the system.

	@Autowired
	ReportsOperationsService reportsOperationsService;

	@PostMapping("/getTradeReport")
	public ResponseEntity<List<ReportsDto>> getTradeReport(@RequestBody TradeReport tradeReport) {
		List<ReportsDto> returnList = new ArrayList<>();
		try {
			
			returnList = reportsOperationsService.processTradeReportRequest(tradeReport);
			
			  if(returnList!=null && returnList.size()>0) {
			  returnList.get(0).setTotalCount(Long.valueOf(returnList.size()));
			  returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			  }
			 
			log.info("Request received for getting trade reports");
			return new ResponseEntity<>(returnList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(returnList, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/export/tradeReport/downloadExcel")
	public void exportToExcel(HttpServletResponse response, @RequestBody TradeReport tradeReport) throws IOException {
		response.setContentType("application/octet-stream");
		List<ReportsDto> returnList = null;
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		
		String headerKey = "Content-Disposition";
		String headerValue ="";
		String sheetName = "";
		if(tradeReport.getReportId()==BigInteger.ONE) {
		headerValue = "attachment; filename=tradereport_" + currentDateTime + ".xlsx";
		sheetName = "TradeReport";
		} else {
			headerValue = "attachment; filename=commissionreport_" + currentDateTime + ".xlsx";
			sheetName = "CommissionReport";
		}
		response.setHeader(headerKey, headerValue);
		try {
			returnList = reportsOperationsService.processTradeReportRequest(tradeReport);
		} catch (ReportNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TradeReportExcelExporter excelExporter = new TradeReportExcelExporter(returnList);
		excelExporter.export(response, sheetName);
	}
	
	
	
}
