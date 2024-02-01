package com.finmamba.simulator.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.kafka.starter.util.ApplicationProperties;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SimulatorOperationsServiceImpl implements SimulatorOperationsService {
	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	SimulatorOperationsServiceDal dal;

	@Autowired
	Gson gson;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	EventPublisher eventPublisher;

	@Override
	public boolean startRateSimulation() {
		
		return false;
	}

	@Override
	public boolean stopRateSimulation() {
		
		return false;
	}

}
