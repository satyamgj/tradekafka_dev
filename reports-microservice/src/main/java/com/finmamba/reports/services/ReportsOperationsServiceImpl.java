package com.finmamba.reports.services;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.NonFlowDto;
import com.finmamba.models.dto.SavedNonFlowDto;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;
import com.finmamba.models.reports.CommissionReport;
import com.finmamba.models.reports.TradeReport;
import com.finmamba.reports.exception.ReportNotFound;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportsOperationsServiceImpl implements ReportsOperationsService {
	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	ReportsOperationsServiceDal dal;

	@Autowired
	Gson gson;

	@Autowired
	ModelMapper modelMapper;

	@Override
	public List<SuperInstitutionalEntityDto> processTradeReportRequest(TradeReport tradeReport) throws ReportNotFound {
		// TODO Auto-generated method stub
		log.info("new TradeReportRequest | START " + Instant.now());
		try {
			/**
			 * Get Trade Report Request from database as per the filters
			 */
			List<SuperInstitutionalEntityDto> requests = dal.getTradeReport(tradeReport);

			return requests;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("new TradeReportRequest | END with empty list" + Instant.now());
		return Collections.emptyList();
	}

	@Override
	public List<SuperInstitutionalEntityDto> processCommissionReportRequest(CommissionReport commissionReport)
			throws ReportNotFound {
		// TODO Auto-generated method stub
		log.info("new CommissionReportRequest | START " + Instant.now());
		try {
			/**
			 * Get Commission Report Request from database as per the filters
			 */
			List<SuperInstitutionalEntityDto> requests = dal.getCommissionReport(commissionReport);

			return requests;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("new CommissionReportRequest | END with empty list" + Instant.now());
		return Collections.emptyList();
	}

	/*
	 * @Override public List<SavedNonFlowDto>
	 * processNonFlowReportRequest(NonFlowRequest nonFlowReport) throws
	 * ReportNotFound { // TODO Auto-generated method stub
	 * log.info("new nonFlowReportRequest | START " + Instant.now()); try {
	 *//**
		 * Get NonFlow Report Request from database as per the filters
		 *//*
			 * List<SavedNonFlowDto> requests = dal.getNonFlowReport(nonFlowReport);
			 * 
			 * return requests; } catch (Exception e) { e.printStackTrace(); }
			 * log.info("new NonFlowReportRequest | END with empty list" + Instant.now());
			 * return Collections.emptyList(); }
			 */
}
