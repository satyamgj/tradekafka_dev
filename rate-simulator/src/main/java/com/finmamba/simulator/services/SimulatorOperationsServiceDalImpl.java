package com.finmamba.simulator.services;

import java.util.List;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.CurrencyPairRates;
import com.finmamba.simulator.repositories.CurrencyPairRatesRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SimulatorOperationsServiceDalImpl implements SimulatorOperationsServiceDal {

	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	CurrencyPairRatesRepository currencyPairRatesRepository;
	
	@Override
	@Transactional
	public List<CurrencyPairRates> getCurrencyPairSpotRates() {
		return currencyPairRatesRepository.getCurrencyPariRates();
	}

}
