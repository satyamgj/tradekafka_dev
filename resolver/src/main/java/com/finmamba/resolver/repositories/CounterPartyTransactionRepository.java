package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.CounterpartyTransactionDetails;

@Repository
public interface CounterPartyTransactionRepository extends JpaRepository<CounterpartyTransactionDetails, Long>{

}
