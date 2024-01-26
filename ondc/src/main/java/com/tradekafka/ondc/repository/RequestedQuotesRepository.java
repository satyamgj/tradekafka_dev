package com.tradekafka.ondc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.ondc.RequestedOrders;
import com.finmamba.entities.transaction.User;

@Repository
public interface RequestedQuotesRepository extends JpaRepository<RequestedOrders, Long> {
	List<RequestedOrders> findByUserId(User user);
}
