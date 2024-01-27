package com.tradekafka.ondc.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.finmamba.models.ondc.OrderForMatchEngine;

@Component
public interface IMatchingService {
	
	void initializeBuyOrderQueue(List<OrderForMatchEngine> buyOrders );
	
	void initializeSellOrderQueue(List<OrderForMatchEngine> sellOrders );

	
	void matchingEngine(OrderForMatchEngine orderForMatchEngine);

	boolean matchConditions(OrderForMatchEngine buyOrder, OrderForMatchEngine sellOrder);

	boolean matchBuyOrder(OrderForMatchEngine buyOrder);

	boolean matchSellOrder(OrderForMatchEngine sellOrder);
	
	 void executeTrade(OrderForMatchEngine buyOrder, OrderForMatchEngine sellOrder, String direction);
	 
	 public String getCurrencyPairKey(OrderForMatchEngine trade);
	 
	 public void removeConfirmedOrder(OrderForMatchEngine order);
	 
	 
}
