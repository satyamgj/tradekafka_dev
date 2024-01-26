package com.tradekafka.ondc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long>{

}
