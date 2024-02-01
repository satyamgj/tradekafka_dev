package com.finmamba.simulator.services;

import java.util.List;

import com.finmamba.entities.staticdata.CurrencyPairRates;

public interface SimulatorOperationsServiceDal {

	public List<CurrencyPairRates> getCurrencyPairSpotRates();
}
