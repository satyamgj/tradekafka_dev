package com.finmamba.simulator.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.CurrencyPairRates;

@Repository
public interface CurrencyPairRatesRepository extends JpaRepository<CurrencyPairRates, Long>{
	
//	@Query(nativeQuery = true, value = "SELECT csr.id"
//			+ ",cs.iso_code as buy_currency"
//			+ ",cd.iso_code as sell_currency"
//			+ ",csr.bid_price as bid_price"
//			+ ",csr.mid_price as mid_price"
//			+ ",csr.ask_price as ask_price"
//			+ ",csr.low_price as low_price"
//			+ ",csr.high_price as high_price"
//			+ " FROM configuration.currencyspotrates csr"
//			+ " join static.currencies cs on cs.id = csr.buy_currency_id"
//			+ " join static.currencies cd on cd.id = csr.sell_currency_id"
//			+ " ORDER BY csr.id ASC")
	@Procedure(procedureName = "configuration.get_currency_pair_spot_rates")
	public List<CurrencyPairRates> getCurrencyPariRates();

}
