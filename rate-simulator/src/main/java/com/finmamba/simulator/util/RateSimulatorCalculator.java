package com.finmamba.simulator.util;

import java.util.Random;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2(topic = "RateCalculator")
public class RateSimulatorCalculator {

	public Pair<Double, Double> spotRateAdjustmentCalculator(double bidRate, double askRate, double preValue) {

		double rnd = myRandom(-0.5, 0.5);
		double randomNumBid = 0.00d;
		double randomNumAsk = 0.00d;
		double halfPercent = ((bidRate + askRate) / 4) * 0.01;
		if (halfPercent <0.1)
		{
			
			halfPercent=halfPercent+0.1;
		}
		// First randomize the direction of up and down

		// We need to generate a number between + and - halfPercent
//		randomNumBid = getRandomValue(rnd, halfPercent, halfPercent+0.4);
//		randomNumAsk = getRandomValue(rnd, halfPercent, halfPercent+0.4);

		randomNumBid = myRandom(0.1, 0.5);
		randomNumAsk = myRandom(0.1, 0.5);

		return Pair.of(randomNumBid, randomNumAsk);

	}

	/**
	 * Generate a decimal string representation of a random number within the
	 * supplied bounds.
	 * 
	 * @param random        the random object (if null, a new one will be created)
	 * @param lowerBound    the lower bound, inclusive
	 * @param upperBound    the upper bound, inclusive
	 * @param decimalPlaces the decimal places of the result
	 * @return the formatted string
	 */
	private double getRandomValue(final double random, final double lowerBound, final double upperBound) {

		if (upperBound <= lowerBound) {
			throw new IllegalArgumentException("Put error message here");
		}

		final double dbl = (random 
				* (upperBound)) + lowerBound;
		log.debug("DBLLL " + dbl);
		return dbl;

	}

	/**
	 * Generate a decimal string representation of a random number within the
	 * supplied bounds.
	 * 
	 * @param random        the random object (if null, a new one will be created)
	 * @param lowerBound    the lower bound, inclusive
	 * @param upperBound    the upper bound, inclusive
	 * @param decimalPlaces the decimal places of the result
	 * @return the formatted string
	 */
	public String getRandomValueWithDecimals(final Random random, final double lowerBound, final double upperBound,
			final int decimalPlaces) {

		if (upperBound <= lowerBound || decimalPlaces < 0) {
			throw new IllegalArgumentException("Put error message here");
		}

		final double dbl = ((random == null ? new Random() : random).nextDouble() //
				* (upperBound - lowerBound)) + lowerBound;
		log.debug("DBLLL " + dbl);
		return String.format("%." + decimalPlaces + "f", dbl);

	}

	double myRandom(double min, double max) {
	    Random r = new Random();
	    return (r.nextInt((int)((max-min)*10+1))+min*10) / 10.0;
	}
}
