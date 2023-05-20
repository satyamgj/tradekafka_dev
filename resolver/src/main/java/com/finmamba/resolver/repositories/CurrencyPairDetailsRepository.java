package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.CurrencyPairDetails;

@Repository
public interface CurrencyPairDetailsRepository extends JpaRepository<CurrencyPairDetails, Long>{
	
	public CurrencyPairDetails findBybaseCurrencyAndQuoteCurrency(Currency base, Currency quote);

}
