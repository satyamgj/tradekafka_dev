package com.finmamba.reports.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.models.dto.NonFlowDto;
import com.finmamba.models.dto.SavedNonFlowDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.reports.CommissionReport;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.nonflow.NonFlowRequest;
import com.finmamba.report.util.CommissionReportExcelExporter;
import com.finmamba.report.util.NonFlowReportExcelExporter;
import com.finmamba.report.util.TradeReportExcelExporter;
import com.finmamba.reports.exception.ReportNotFound;
import com.finmamba.reports.services.ReportsOperationsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
@Slf4j
public class ReportController {

	@Autowired
	ReportsOperationsService reportsOperationsService;

	@PostMapping("/getTradeReport")
	public ResponseEntity<List<SuperInstitutionalEntityDto>> getTradeReport(@RequestBody TradeReport tradeReport) {
		List<SuperInstitutionalEntityDto> returnList = new ArrayList<>();
		try {
			
			returnList = reportsOperationsService.processTradeReportRequest(tradeReport);
			returnList.get(0).setTotal_count(Long.valueOf(returnList.size()));
			log.info("Request received for getting trade reports");
			// returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			return new ResponseEntity<>(returnList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(returnList, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/getCommissionReport")
	public ResponseEntity<List<SuperInstitutionalEntityDto>> getCommissionReport(@RequestBody CommissionReport commissionReport) {
		List<SuperInstitutionalEntityDto> returnList = new ArrayList<>();
		try {
			returnList = reportsOperationsService.processCommissionReportRequest(commissionReport);
			log.info("Request received for getting commission reports");
			returnList.get(0).setTotal_count(Long.valueOf(returnList.size()));

			
			// returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			return new ResponseEntity<>(returnList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(returnList, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/getNonFlowReport")
	public ResponseEntity<List<SavedNonFlowDto>> getNonFlowReport(@RequestBody NonFlowRequest nonFlowRequest) {
		List<SavedNonFlowDto> returnList = new ArrayList<>();
		try {
			log.info("Request received for getting nonFlow reports");
			returnList = reportsOperationsService.processNonFlowReportRequest(nonFlowRequest);
			// returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			returnList.get(0).setTotal_count(Long.valueOf(returnList.size()));

			return new ResponseEntity<>(returnList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(returnList, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/export/nonFlowReport/downloadExcel")
	public ResponseEntity<List<SavedNonFlowDto>> exportNonFlowReportToExcel(HttpServletResponse response, @RequestBody NonFlowRequest nonFlowRequest) {
		List<SavedNonFlowDto> returnList = null;
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=nonflowreport_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);

		try {
			log.info("Request received for getting nonFlow reports");
			returnList = reportsOperationsService.processNonFlowReportRequest(nonFlowRequest);
			// returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			NonFlowReportExcelExporter excelExporter = new NonFlowReportExcelExporter(returnList);
			excelExporter.export(response);
			return new ResponseEntity<>(returnList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(returnList, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/export/tradeReport/downloadExcel")
	public void exportToExcel(HttpServletResponse response, @RequestBody TradeReport tradeReport) throws IOException {
		response.setContentType("application/octet-stream");
		List<SuperInstitutionalEntityDto> returnList = null;
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=tradereport_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		try {
			returnList = reportsOperationsService.processTradeReportRequest(tradeReport);
		} catch (ReportNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TradeReportExcelExporter excelExporter = new TradeReportExcelExporter(returnList);
		excelExporter.export(response);
	}
	
	
	@PostMapping("/export/commissionReport/downloadExcel")
	public void exportCommissionReportToExcel(HttpServletResponse response, @RequestBody CommissionReport commissionReport) throws IOException {
		response.setContentType("application/octet-stream");
		List<SuperInstitutionalEntityDto> returnList = null;
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=commissionreport_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		try {
			returnList = reportsOperationsService.processCommissionReportRequest(commissionReport);
		} catch (ReportNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CommissionReportExcelExporter excelExporter = new CommissionReportExcelExporter(returnList);
		excelExporter.export(response);
	}
	
}
