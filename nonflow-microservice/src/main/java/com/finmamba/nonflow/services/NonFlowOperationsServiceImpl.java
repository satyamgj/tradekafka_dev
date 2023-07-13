package com.finmamba.nonflow.services;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import com.finmamba.entities.transaction.Daddy;
import com.finmamba.entities.transaction.NonFlow;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.DaddyDto;
import com.finmamba.models.dto.NonFlowDto;
import com.finmamba.models.dto.SavedNonFlowDto;
//import com.finmamba.nonflow.NonFlowRequest;
import com.finmamba.nonflow.exception.CustomNonFlowException;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NonFlowOperationsServiceImpl implements NonFlowOperationsService {
	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	NonFlowOperationsServiceDal dal;

	@Autowired
	Gson gson;

	@Autowired
	ModelMapper modelMapper;

	@Override
	public List<SavedNonFlowDto> saveNonFlowTrade(NonFlowDto nonFlowDto) {

		log.info("Save non flow Request | START " + Instant.now());
		try {
			/**
			 * Save non flow Request in database
			 */

			List<SavedNonFlowDto> requests = dal.saveNonFlowRequest(nonFlowDto);

			/**
			 * Return the list of non flow Request requests for this user to frontend
			 */
			log.info("Save non flow Request | END" + Instant.now());

			return requests;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Save non flow Request | END with empty list" + Instant.now());
		return null;

	}

	/*
	 * @Override public List<NonFlow> getNonFlowTrades(NonFlowRequest
	 * nonFlowRequest) {
	 * 
	 * log.info("Get non flow Request | START " + Instant.now()); try {
	 *//**
		 * Get non flow Request in database
		 */
	/*
	 * 
	 * List<NonFlow> requests = dal.getNonFlowRequest(nonFlowRequest);
	 * 
	 *//**
		 * Return the list of non flow Request requests for this user to frontend
		 *//*
			 * log.info("Get non flow Request | END" + Instant.now());
			 * 
			 * return requests; } catch (Exception e) { e.printStackTrace(); }
			 * log.info("Get non flow Request | END with empty list" + Instant.now());
			 * return Collections.emptyList();
			 * 
			 * }
			 * 
			 */
}
