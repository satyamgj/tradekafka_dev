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

import com.finmamba.entities.transaction.ConfirmedOrderDetails;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.reports.CommissionReport;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.reports.exception.ReportNotFound;
import com.finmamba.reports.repositories.CommissionReportsRepository;
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
	
	@Autowired
	NonFlowReportRepository nonFlowReportsRepository;

	@Autowired
	CommissionReportsRepository commissionReportsRepository;

	@Override
	@Transactional
	public List<SuperInstitutionalEntityDto> getTradeReport(TradeReport tradeReport) {
		log.info("Inside getTradeReport Dal Implementation For Banks");
		List<SuperInstitutionalEntityDto> returnList = null;
		BigInteger reportId = tradeReport.getReportId();
		int pageSize = tradeReport.getPageSize().intValue();
		int offSet = tradeReport.getOffset().intValue();
		String sortField = tradeReport.getSortField();
		BigInteger institutionId = tradeReport.getEntityId();
		String startDate = tradeReport.getStartDate();
		String endDate = tradeReport.getEndDate();
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
		
		java.sql.Date sqlEndDate = null; 
		java.sql.Date sqlStartDate = null; 
		
		try {
			java.util.Date utilStartDate = sdf1.parse(startDate);
			java.util.Date utilEndDate = sdf1.parse(endDate);
			sqlEndDate = new java.sql.Date(utilEndDate.getTime()); 
			sqlStartDate = new java.sql.Date(utilStartDate.getTime()); 
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

		Type listType = new TypeToken<List<SuperInstitutionalEntityDto>>() {
		}.getType();
		log.info("tradeReport.getReportId()" + reportId);
			List<ConfirmedOrderDetails> tradeReportList = tradeReportsRepository
					.getAllBetweenDates( sqlStartDate, sqlEndDate,false, pageableRequest);
			log.info("return list of Banks" + tradeReportList);
			returnList = modelMapper.map(tradeReportList, listType);
		
		return returnList;
	}

	@Override
	@Transactional
	public List<SuperInstitutionalEntityDto> getCommissionReport(CommissionReport commissionReport) throws ReportNotFound {
		log.info("Inside getCommissionReport Dal Implementation");
		List<SuperInstitutionalEntityDto> returnList = null;
		BigInteger reportId = commissionReport.getReportId();
		int pageSize = commissionReport.getPageSize().intValue();
		int offSet = commissionReport.getOffset().intValue();
		String sortField = commissionReport.getSortField();
		BigInteger institutionId = commissionReport.getEntityId();
		String startDate = commissionReport.getStartDate();
		String endDate = commissionReport.getEndDate();
		
		// Make Pageable request as per the sort order
		Pageable pageableRequest = null;
		if (commissionReport.getSortOrder().equalsIgnoreCase("asc")) {
			pageableRequest = PageRequest.of(offSet, pageSize,
					Sort.Direction.ASC, sortField);
		} else {
			pageableRequest = PageRequest.of(offSet, pageSize,
					Sort.Direction.DESC, sortField);
		}

		Type listType = new TypeToken<List<SuperInstitutionalEntityDto>>() {
		}.getType();
		log.info("CommissionReport.getReportId()", reportId);
		if (commissionReport.getEntityId() == BigInteger.valueOf(3)) {
			
//			log.info(tradeReportsRepository
//					.getCommissionReportFromEntity(reportId, institutionId, startDate, endDate, pageableRequest).toString());
			
			List<ConfirmedOrderDetails> kanjiList = commissionReportsRepository
					.getCommissionReportFromEntity(reportId, institutionId, startDate, endDate, pageableRequest);
			log.info("return list of Kanji world" + kanjiList);
			returnList = modelMapper.map(
					commissionReportsRepository.getCommissionReportFromEntity(reportId, institutionId, startDate, endDate, pageableRequest)
										, listType);
		}
		return returnList;
	}

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
