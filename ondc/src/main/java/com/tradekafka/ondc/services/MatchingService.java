package com.tradekafka.ondc.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.finmamba.entities.ondc.MatchingTransactions;
import com.finmamba.entities.ondc.QuoteRequest;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.ondc.MatchNotification;
import com.finmamba.models.ondc.OrderForMatchEngine;
import com.tradekafka.ondc.repository.MatchingTransactionRepository;
import com.tradekafka.ondc.repository.QuoteRequestRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchingService implements IMatchingService {

	@Autowired
	QuoteRequestRepository quoteRequestRepository;

	@Autowired
	MatchingTransactionRepository matchingTransactionRepository;
	
	@Autowired
	KafkaProducerService kafkaProducerService;
	
	private static final String topic = "notification";
	


	static final Long BUYID = 1L;
	static final Long SELLID = 2L;

	private Map<String, List<OrderForMatchEngine>> buyOrdersMap; // Map for buy orders indexed by currency pair
	private Map<String, List<OrderForMatchEngine>> sellOrdersMap; // Map for sell orders indexed by currency pair

	public MatchingService() {
		this.buyOrdersMap = new HashMap<>();
		this.sellOrdersMap = new HashMap<>();
	}

	@Async
	@Override
	public void matchingEngine(OrderForMatchEngine orderForMatchEngine) {
		// TODO Auto-generated method stub
		startTime("matchingEngine");
		try {
			String key = orderForMatchEngine.getBaseCurrency() + orderForMatchEngine.getQuoteCurrency();

			if (orderForMatchEngine.getDirectionId() == BUYID) {
				log.info("Received a new buy order for ccy pair " + orderForMatchEngine.getBaseCurrency() + "/"
						+ orderForMatchEngine.getQuoteCurrency());

				// First will let go this order via matching algo
				Boolean matched = matchBuyOrder(orderForMatchEngine);

				if (!matched) {
					List<OrderForMatchEngine> buyOrders = buyOrdersMap.get(key);
					if(buyOrders !=null) {
						buyOrders.add(orderForMatchEngine);
						buyOrdersMap.put(key, buyOrders);
					}else {
						List<OrderForMatchEngine>newList = new ArrayList<>();
						newList.add(orderForMatchEngine);
						buyOrdersMap.put(key, newList);

					}
				}

			} else {
				log.info("Received a new sell order for ccy pair " + orderForMatchEngine.getBaseCurrency() + "/"
						+ orderForMatchEngine.getQuoteCurrency());

				// First will let go this order via matching algo
				Boolean matched = matchSellOrder(orderForMatchEngine);

				if (!matched) {

					List<OrderForMatchEngine> sellOrders = sellOrdersMap.get(key);
					if(sellOrders!=null) {
						sellOrders.add(orderForMatchEngine);
						sellOrdersMap.put(key, sellOrders);
					}else {
						List<OrderForMatchEngine>newList = new ArrayList<>();
						newList.add(orderForMatchEngine);
						sellOrdersMap.put(key, newList);
					}
				}
			}

			endTime("matchingEngine");

			
		} catch (Exception e) {
			
			log.info("Exception occured in matchingEngine -> " + e.getMessage());
			endTime("matchingEngine");

		}

	}

	@Override
	public boolean matchBuyOrder(OrderForMatchEngine buyOrder) {

		boolean matched = false;
		
		String key =  buyOrder.getBaseCurrency()+buyOrder.getQuoteCurrency() ;


		List<OrderForMatchEngine> sellOrders = sellOrdersMap.get(key);
		if (sellOrders != null) {
			ListIterator<OrderForMatchEngine> sellIterator = sellOrders.listIterator();
			while (sellIterator.hasNext()) {
				OrderForMatchEngine sellOrder = sellIterator.next();
				if (matchConditions(buyOrder, sellOrder)) {
					matched = true;
					executeTrade(buyOrder, sellOrder, "BUY");
					// sellIterator.remove();
					// break;
				}
			}
		}

		return matched;
	}

	@Override
	public boolean matchSellOrder(OrderForMatchEngine sellOrder) {

		boolean matched = false;
		
		String key =  sellOrder.getBaseCurrency()+sellOrder.getQuoteCurrency();


		List<OrderForMatchEngine> buyOrders = buyOrdersMap.get(key);
		if (buyOrders != null) {
			ListIterator<OrderForMatchEngine> buyIterator = buyOrders.listIterator();
			while (buyIterator.hasNext()) {
				OrderForMatchEngine buyOrder = buyIterator.next();
				if (matchConditions(buyOrder, sellOrder)) {
					matched = true;

					executeTrade(buyOrder, sellOrder, "SELL");
					// buyIterator.remove();
					// break;
				}
			}
		}

		return matched;
	}

	@Override
	public boolean matchConditions(OrderForMatchEngine buyOrder, OrderForMatchEngine sellOrder) {
		return 	buyOrder.getInitiatorId() != sellOrder.getInitiatorId()
				&& buyOrder.getBaseCurrency().equals(sellOrder.getBaseCurrency())
				&& buyOrder.getQuoteCurrency().equals(sellOrder.getQuoteCurrency())
				&& buyOrder.getAskingRate().compareTo(sellOrder.getAskingRate()) >= 0
				&& buyOrder.getAmount().compareTo(sellOrder.getAmount()) >= 0
				;
	}

	@Transactional
	@Override
	public void executeTrade(OrderForMatchEngine buyOrder, OrderForMatchEngine sellOrder, String direction) {
		// Execute the trade logic here
		try {
			System.out.println("Trade Matched - Buy: " + buyOrder.getAmount() + " " + buyOrder.getBaseCurrency()
					+ ", Sell: " + sellOrder.getAmount() + " " + sellOrder.getBaseCurrency() + " at rate "
					+ sellOrder.getAskingRate());
			
			String message = "Trade Matched - Buy: " + buyOrder.getAmount() + " " + buyOrder.getBaseCurrency()
			+ ", Sell: " + sellOrder.getAmount() + " " + sellOrder.getBaseCurrency() + " at rate "
			+ sellOrder.getAskingRate();
			
			
			
			log.info("buyorderId -> " + buyOrder.getQuoteRequestId() + " sellQuote->" + sellOrder.getQuoteRequestId() );

			log.info("Adding trades in matching table");
			
			if( quoteRequestRepository.existsById(sellOrder.getQuoteRequestId()) && quoteRequestRepository.existsById(buyOrder.getQuoteRequestId()) )
			{
				
			
			QuoteRequest buyQuote = quoteRequestRepository.findById(buyOrder.getQuoteRequestId()).get();
			QuoteRequest sellQuote = quoteRequestRepository.findById(sellOrder.getQuoteRequestId()).get();

			if (!buyQuote.getIsMatched()) {
				buyQuote.setIsMatched(true);
				quoteRequestRepository.saveAndFlush(buyQuote);
			}
			if (!sellQuote.getIsMatched()) {
				sellQuote.setIsMatched(true);
				quoteRequestRepository.saveAndFlush(sellQuote);
			}

			if(!matchingTransactionRepository.existsByBuyTxnIdAndSellTxnId(buyQuote, sellQuote)) {
				MatchingTransactions newMatch = MatchingTransactions.builder().buyTxnId(buyQuote).sellTxnId(sellQuote)
						.isActive(true)
						.build();
				matchingTransactionRepository.save(newMatch);
			}
			
			
//sending notification to buyer
			
			MatchNotification notificationToBuyOrder =  MatchNotification.builder()
					.message(message)
					.userId(buyOrder.getInitiatorId())
					.build()
					;
			
			kafkaProducerService.send(notificationToBuyOrder, topic, null, null);

			
		//sending notification to seller
			
			MatchNotification notificationToSellOrder =  MatchNotification.builder()
					.message(message)
					.userId(sellOrder.getInitiatorId())
					.build()
					;
			
			kafkaProducerService.send(notificationToSellOrder, topic, null, null);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Exception occured in executeTrade -> " + e.getMessage());

		}

	}

	@Override
	public String getCurrencyPairKey(OrderForMatchEngine trade) {
		return trade.getBaseCurrency() + "-" + trade.getQuoteCurrency();
	}

	@Override
	public void removeConfirmedOrder(OrderForMatchEngine orderForMatchEngine) {
		try {
			String key = orderForMatchEngine.getBaseCurrency() + orderForMatchEngine.getQuoteCurrency();

			if (orderForMatchEngine.getDirectionId() == BUYID) {
				log.info("Removing buy order for ccy pair " + orderForMatchEngine.getBaseCurrency() + "/"
						+ orderForMatchEngine.getQuoteCurrency() + " as it is confirmed. ");


				
					List<OrderForMatchEngine> buyOrders = buyOrdersMap.get(key);
					if(buyOrders !=null) {
						for(OrderForMatchEngine order : buyOrders) {
							if(order.getQuoteRequestId().equals(orderForMatchEngine.getQuoteRequestId())) {
								//remove
								buyOrders.remove(order);
							}
							
						}
					}
				

			} else {
				log.info("Removing a sell order for ccy pair " + orderForMatchEngine.getBaseCurrency() + "/"
						+ orderForMatchEngine.getQuoteCurrency());



					List<OrderForMatchEngine> sellOrders = sellOrdersMap.get(key);
					if(sellOrders!=null) {
						for(OrderForMatchEngine order : sellOrders) {
							if(order.getQuoteRequestId().equals(orderForMatchEngine.getQuoteRequestId())) {
								//remove
								sellOrders.remove(order);
							}
							
						}
					}
				
			}

		} catch (Exception e) {
			log.info("Exception occured in matchingEngine -> " + e.getMessage());
		}
		
	}
	static void startTime(String funcName) {
		// Get the current date and time
		LocalDateTime currentDateTime = LocalDateTime.now();

		// Define a format for displaying the date and time
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// Format the current date and time using the defined format
		String formattedDateTime = currentDateTime.format(formatter);

		log.info(funcName + " started at - > " + formattedDateTime);
	}

	static void endTime(String funcName) {
		// Get the current date and time
		LocalDateTime currentDateTime = LocalDateTime.now();

		// Define a format for displaying the date and time
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// Format the current date and time using the defined format
		String formattedDateTime = currentDateTime.format(formatter);
		log.info(funcName + " ended at - > " + formattedDateTime);

	}

	@Override
	public void initializeBuyOrderQueue(List<OrderForMatchEngine> buyOrders) {
		try {
			
			startTime("initializeBuyOrderQueue");
			
			for(OrderForMatchEngine order : buyOrders) {
			
				String key = order.getBaseCurrency() + order.getQuoteCurrency();
				List<OrderForMatchEngine> buyOrdersForCcy = buyOrdersMap.get(key);
				if(buyOrdersForCcy !=null) {
					buyOrdersForCcy.add(order);
					buyOrdersMap.put(key, buyOrders);
				}else {
					List<OrderForMatchEngine>newList = new ArrayList<>();
					newList.add(order);
					buyOrdersMap.put(key, newList);

				}
			}
			
			endTime("initializeBuyOrderQueue");
			
		} catch (Exception e) {
			
			log.info("exception occured inside initializeBuyOrderQueue " + e.getMessage());

		}
		
	}

	@Override
	public void initializeSellOrderQueue(List<OrderForMatchEngine> sellOrders) {

		try {
			startTime("initializeSellOrderQueue");
			
			for(OrderForMatchEngine order : sellOrders) {
			
				String key = order.getBaseCurrency() + order.getQuoteCurrency();
				List<OrderForMatchEngine> sellOrdersForCcy = sellOrdersMap.get(key);
				if(sellOrdersForCcy !=null) {
					sellOrdersForCcy.add(order);
					sellOrdersMap.put(key, sellOrders);
				}else {
					List<OrderForMatchEngine>newList = new ArrayList<>();
					newList.add(order);
					sellOrdersMap.put(key, newList);

				}
			}
			
			endTime("initializeSellOrderQueue");
		} catch (Exception e) {

			log.info("exception occured inside initializeSellOrderQueue " + e.getMessage());
		}
		
	}
}
