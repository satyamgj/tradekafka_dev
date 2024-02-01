package com.finmamba.simulator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.simulator.services.SimulatorOperationsService;
import com.finmamba.simulator.util.ApplicationConstants;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/simulator")
@CrossOrigin(origins = "*")
@Slf4j
public class Controller {

	@Autowired
	SimulatorOperationsService simulatorOperationsService;
	
	@PostMapping("/enableRateStream")
	public ResponseEntity<Boolean> enableRateStream() {
		try {
			log.info("Starting rate simulation");
			ApplicationConstants.setRateSimulationStatus(true);
			return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/disableRateStream")
	public ResponseEntity<Boolean> disableRateStream() {
		try {
			log.info("Stopping rate stream");
			ApplicationConstants.setRateSimulationStatus(true);
			return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
