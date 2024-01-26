package com.tradekafka.ondc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.ondc.SellerCatalogue;
import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.transaction.User;

@Repository
public interface SellerCatalogueRepository extends JpaRepository<SellerCatalogue, Long> {
	List<SellerCatalogue> findByUserId(User user);
	List<SellerCatalogue> findByBaseCurrencyAndQuoteCurrency(Currency baseCcy, Currency sellCcy);
}
