package com.finmamba.models.simulator;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class SimulatedSpotRates  {
	
	@NonNull
    int buyCurrencyId;
	@NonNull
    int sellCurrencyId;
    int timeInSeconds;
}
