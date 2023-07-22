package com.finmamba.nonflow.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
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

import com.finmamba.models.reports.NonFlowReportDto;
import com.finmamba.models.reports.SaveNonFlowDto;
import com.finmamba.models.reports.SearchNonFlowDto;
import com.finmamba.nonflow.services.NonFlowOperationsService;
import com.finmamba.nonflow.util.NonFlowReportExcelExporter;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/nonflow")
@CrossOrigin(origins = "*")
@Slf4j
public class NonFlowController {

	@Autowired
	NonFlowOperationsService nonFlowOperationsService;

	@PostMapping("/saveNonFlowTrade")
	public ResponseEntity<List<SaveNonFlowDto>> processQuoteRequest(@RequestBody SaveNonFlowDto nonFlowModel) {

		try {
			List<SaveNonFlowDto> returnList = nonFlowOperationsService.saveNonFlowTrade(nonFlowModel);

			if (returnList!=null && returnList.size() > 0) {
				returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			}
			return new ResponseEntity<>(returnList, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/getNonFlowTrades")
	public ResponseEntity<List<NonFlowReportDto>> getNonFlowTrades(@RequestBody SearchNonFlowDto nonFlowRequest) {
		List<NonFlowReportDto> returnList = null;
		try {
			returnList = nonFlowOperationsService.getNonFlowTrades(nonFlowRequest);
			log.info("Request received for getting non flow trades"); //
			if (returnList.size() > 0) {
			returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			}
			return new ResponseEntity<>(returnList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(returnList, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/export/nonFlowReport/downloadExcel")
	public void exportToExcel(HttpServletResponse response, @RequestBody SearchNonFlowDto nonFlowRequest) throws IOException {
		response.setContentType("application/octet-stream");
		List<NonFlowReportDto> returnList = null;
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		
		String headerKey = "Content-Disposition";
		String headerValue ="";
		String sheetName = "";
		headerValue = "attachment; filename=externaltradereport_" + currentDateTime + ".xlsx";
		sheetName = "External Trade Report";
		response.setHeader(headerKey, headerValue);
		try {
			returnList = nonFlowOperationsService.getNonFlowTrades(nonFlowRequest);
			//returnList = reportsOperationsService.processTradeReportRequest(tradeReport);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NonFlowReportExcelExporter excelExporter = new NonFlowReportExcelExporter(returnList);
		excelExporter.export(response, sheetName);
	}

}
