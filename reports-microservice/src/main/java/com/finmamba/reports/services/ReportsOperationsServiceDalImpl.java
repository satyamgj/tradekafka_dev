package com.finmamba.reports.services;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.finmamba.entities.reports.Reports;
import com.finmamba.models.dto.ReportsDto;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.reports.repositories.NonFlowReportRepository;
import com.finmamba.reports.repositories.TradeReportsRepository;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportsOperationsServiceDalImpl implements ReportsOperationsServiceDal {

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	TradeReportsRepository tradeReportsRepository;
@Override
@Transactional
public List<ReportsDto> getTradeReport(TradeReport tradeReport) {
	log.info("Inside getTradeReport Dal Implementation For Banks");
	List<ReportsDto> returnList = null;
	BigInteger reportId = tradeReport.getReportId();
	int pageSize = tradeReport.getPageSize().intValue();
	int offSet = tradeReport.getOffset().intValue();
	String sortField = tradeReport.getSortField();
	BigInteger institutionId = tradeReport.getEntityId();
	String startDate = tradeReport.getStartDate();
	String endDate = tradeReport.getEndDate();
	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
	java.util.Date utilStartDate = null;
	java.util.Date utilEndDate = null;
	java.sql.Date sqlStartDate = null; 
	java.sql.Date sqlEndDate =null;
	try {
		utilStartDate = sdf1.parse(startDate);
		utilEndDate = sdf1.parse(endDate);
		sqlStartDate = new java.sql.Date(utilStartDate.getTime()); 
		sqlEndDate = new java.sql.Date(utilEndDate.getTime()); 
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	// Make Pageable request as per the sort order
	Pageable pageableRequest = null;
	if (tradeReport.getSortOrder().equalsIgnoreCase("asc")) {
		pageableRequest = PageRequest.of(offSet, pageSize,
				Sort.Direction.ASC, sortField);
	} else {
		pageableRequest = PageRequest.of(offSet, pageSize,
				Sort.Direction.DESC, sortField);
	}

	Type listType = new TypeToken<List<ReportsDto>>() {
	}.getType();
	log.info("tradeReport.getReportId()" + reportId);
		List<Reports> tradeReportList = tradeReportsRepository
				.getReportFromEntity( reportId.intValue(), institutionId.intValue(), sqlStartDate, sqlEndDate, pageableRequest);
		log.info("return list of Banks" + tradeReportList);
		returnList = modelMapper.map(tradeReportList, listType);
	return returnList;
}
	@Autowired
	NonFlowReportRepository nonFlowReportsRepository;

	
	/*
	 * @Override
	 * 
	 * @Transactional public List<SavedNonFlowDto> getNonFlowReport(NonFlowRequest
	 * nonFlowRequest) throws ReportNotFound {
	 * log.info("Inside get Non Flow Dal Implementation"); List<SavedNonFlowDto>
	 * returnList = null; int pageSize = nonFlowRequest.getPageSize().intValue();
	 * int offSet = nonFlowRequest.getOffset().intValue(); String sortField =
	 * nonFlowRequest.getSortField(); BigInteger institutionId =
	 * nonFlowRequest.getEntityId(); String startDate =
	 * nonFlowRequest.getStartDate(); String endDate = nonFlowRequest.getEndDate();
	 * 
	 * // Make Pageable request as per the sort order Pageable pageableRequest =
	 * null; if (nonFlowRequest.getSortOrder().equalsIgnoreCase("asc")) {
	 * pageableRequest = PageRequest.of(offSet, pageSize, Sort.Direction.ASC,
	 * sortField); } else { pageableRequest = PageRequest.of(offSet, pageSize,
	 * Sort.Direction.DESC, sortField); }
	 * 
	 * Type listType = new TypeToken<List<SavedNonFlowDto>>() { }.getType();
	 * 
	 * List<NonFlow> nonFlowList = nonFlowReportsRepository
	 * .getNonFlowRequest(institutionId, startDate, endDate, pageableRequest);
	 * log.info("return list of NonFlow requests" + nonFlowList); returnList =
	 * modelMapper.map(nonFlowList, listType);
	 * 
	 * return returnList; }
	 */
}
