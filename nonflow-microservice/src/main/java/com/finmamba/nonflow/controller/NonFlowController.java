package com.finmamba.nonflow.controller;

import java.util.Collections;
import java.util.List;

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
import com.finmamba.nonflow.services.NonFlowOperationsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/nonflow")
@CrossOrigin(origins = "*")
@Slf4j
public class NonFlowController {

	@Autowired
	NonFlowOperationsService nonFlowOperationsService;

	@PostMapping("/saveNonFlowTrade")
	public ResponseEntity<List<SavedNonFlowDto>> processQuoteRequest(@RequestBody NonFlowDto nonFlowModel) {

		try {
			List<SavedNonFlowDto> returnList = nonFlowOperationsService.saveNonFlowTrade(nonFlowModel);

			if (returnList.size() > 0) {
				returnList.get(0).setHttpStatus(HttpStatus.OK.value());
			}
			return new ResponseEntity<>(returnList, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * @PostMapping("/getNonFlowTrades") public ResponseEntity<List<NonFlow>>
	 * getNonFlowTrades(@RequestBody NonFlowRequest nonFlowRequest) { List<NonFlow>
	 * returnList = null; try { returnList =
	 * nonFlowOperationsService.getNonFlowTrades(nonFlowRequest);
	 * log.info("Request received for getting non flow trades"); //
	 * returnList.get(0).setHttpStatus(HttpStatus.OK.value()); return new
	 * ResponseEntity<>(returnList, HttpStatus.OK); } catch (Exception e) {
	 * e.printStackTrace(); return new ResponseEntity<>(returnList,
	 * HttpStatus.INTERNAL_SERVER_ERROR); } }
	 */
}
