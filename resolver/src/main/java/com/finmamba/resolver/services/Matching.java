package com.finmamba.resolver.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.matching.NewOrder;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Matching {

	@Autowired
	CounterPartyTransactionRepository counterPartyTransactionRepository;

	@Autowired
	KafkaProducerService kafkaProducerService;
	
	BigDecimal zero = new BigDecimal(0);

	Map<BigDecimal, List<NewOrder>> buyOrders;
	Map<BigDecimal, List<NewOrder>> sellOrders;

	public Matching() {
		buyOrders = new TreeMap<>(Collections.reverseOrder());
		sellOrders = new TreeMap<>();
	}

	@Async
	public void initiateMatchingAlgorithm(NewOrder order) {

		log.info("Received a new order");

		if (order.getBuyAmount().compareTo(zero) <= 0 || order.getSellAmount().compareTo(zero) <= 0) {
			return;
		}

		if (order.getDirection().getId() == 1) {
			buyOrders.computeIfAbsent(order.getAskingRate(), k -> new ArrayList<>()).add(order);
			matchOrders(sellOrders, order);
		} else {
			sellOrders.computeIfAbsent(order.getAskingRate(), k -> new ArrayList<>()).add(order);
			matchOrders(buyOrders, order);
		}
	}

	public void matchOrders(Map<BigDecimal, List<NewOrder>> oppositeOrders, NewOrder newOrder) {

		List<BigDecimal> oppositePrices = new ArrayList<>(oppositeOrders.keySet());
		List<NewOrder> matchedOrders = new ArrayList<>();

		CounterpartyTransactionDetails newOrderTransaction = counterPartyTransactionRepository
				.findById(newOrder.getOrderId()).get();

		for (BigDecimal price : oppositePrices) {
		
			List<NewOrder> oppositeOrderList = oppositeOrders.get(price);

			// Iterate over the list using an iterator to safely remove elements
			Iterator<NewOrder> iterator = oppositeOrderList.iterator();
			while (iterator.hasNext()) {
				NewOrder oppositeOrder = iterator.next();
			
				// checking whether destination is same then only matching
				CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
						.findById(oppositeOrder.getOrderId()).get();

				if (newOrderTransaction.getDestinationId().getId() == oppositeOrderTransaction.getDestinationId()
						.getId() && oppositeOrderTransaction.isRequestActive()) {

					// Check if new Order buy condition and asking rate comparison
					if (newOrder.getDirection().getId() == 1
							&& (newOrder.getBuyCurrency().getId() == oppositeOrder.getBuyCurrency().getId()
									&& newOrder.getSellCurrency().getId() == oppositeOrder.getSellCurrency().getId())
							&& (oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == -1
									|| oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 0)) {

						// Case 1: both isPartial true
						if (newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
							
							String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
							
							log.info(matchMessage);
							
							log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);
							
							iterator.remove();
							matchedOrders.add(oppositeOrder);

							List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

							if (orderList != null) {
								orderList.remove(newOrder);
								if (orderList.isEmpty()) {
									oppositeOrders.remove(newOrder.getAskingRate());
								}
							}
							buyOrders.remove(newOrder.getAskingRate());
							buyOrders.put(newOrder.getAskingRate(), matchedOrders);
							break;

						}
						// case 2: only buy order is partial allowed and not sell order not
						else if (newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
							if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0
									|| oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == -1) {
								// i.e match -> if buy amount equals with sell order buy amount
								// & if buy amt is greater than sell order amount

								String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
										+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
										+ oppositeOrder.getAskingRate();

								String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
								
								log.info(matchMessage);
								
								log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

								kafkaProducerService.send(matchMessage, topic, null, null);

								
								iterator.remove();
								matchedOrders.add(oppositeOrder);

								List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

								if (orderList != null) {
									orderList.remove(newOrder);
									if (orderList.isEmpty()) {
										oppositeOrders.remove(newOrder.getAskingRate());
									}
								}
								buyOrders.remove(newOrder.getAskingRate());
								buyOrders.put(newOrder.getAskingRate(), matchedOrders);
								break;
							}

						}
						// case 3: only sell order is partial fill allowed.
						else if (!newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
							if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0
									|| oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 1) {
								// i.e match -> if buy amount equals with sell order buy amount
								// & if buy amt is less than sell order amount

								String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
										+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
										+ oppositeOrder.getAskingRate();

								String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
								
								log.info(matchMessage);
								
								log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

								kafkaProducerService.send(matchMessage, topic, null, null);

								
								iterator.remove();
								matchedOrders.add(oppositeOrder);

								List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

								if (orderList != null) {
									orderList.remove(newOrder);
									if (orderList.isEmpty()) {
										oppositeOrders.remove(newOrder.getAskingRate());
									}
								}
								buyOrders.remove(newOrder.getAskingRate());
								buyOrders.put(newOrder.getAskingRate(), matchedOrders);
								break;
							}
						}
						// case 4: both partial fill not allowed
						else if (!newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
							if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0) {
								// i.e match -> if buy amount equals with sell order buy amount

								String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
										+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
										+ oppositeOrder.getAskingRate();

								String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
								
								log.info(matchMessage);
								
								log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

								kafkaProducerService.send(matchMessage, topic, null, null);

								
								iterator.remove();
								matchedOrders.add(oppositeOrder);
								

								List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

								if (orderList != null) {
									orderList.remove(newOrder);
									if (orderList.isEmpty()) {
										oppositeOrders.remove(newOrder.getAskingRate());
									}
								}
								buyOrders.remove(newOrder.getAskingRate());
								buyOrders.put(newOrder.getAskingRate(), matchedOrders);
								break;
							}

						}

					}
					// Check if new Order is a sell order
					else if (newOrder.getDirection().getId() == 2
							&& (newOrder.getBuyCurrency().getId() == oppositeOrder.getBuyCurrency().getId()
									&& newOrder.getSellCurrency().getId() == oppositeOrder.getSellCurrency().getId())
							&& (oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 1
									|| oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 0)) {

						// Case 1: both isPartial true
						if (newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {

							String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
							
							log.info(matchMessage);
							
							log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);


							iterator.remove();
							matchedOrders.add(oppositeOrder);

							List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

							if (orderList != null) {
								orderList.remove(newOrder);
								if (orderList.isEmpty()) {
									oppositeOrders.remove(newOrder.getAskingRate());
								}
							}
							sellOrders.remove(newOrder.getAskingRate());
							sellOrders.put(newOrder.getAskingRate(), matchedOrders);
							break;

						}
						// case 2: only buy order is partial allowed and not sell order not
						else if (newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
							if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0
									|| oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == -1) {
								// i.e match -> if buy amount equals with sell order buy amount
								// & if buy amt is greater than sell order amount

								String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
										+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
										+ oppositeOrder.getAskingRate();

								String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
								
								log.info(matchMessage);
								
								log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

								kafkaProducerService.send(matchMessage, topic, null, null);

								iterator.remove();
								matchedOrders.add(oppositeOrder);

								List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

								if (orderList != null) {
									orderList.remove(newOrder);
									if (orderList.isEmpty()) {
										oppositeOrders.remove(newOrder.getAskingRate());
									}
								}
								sellOrders.remove(newOrder.getAskingRate());
								sellOrders.put(newOrder.getAskingRate(), matchedOrders);
								break;
							}

						}
						// case 3: only sell order is partial fill allowed.
						else if (!newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
							if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0
									|| oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 1) {
								// i.e match -> if buy amount equals with sell order buy amount
								// & if buy amt is less than sell order amount

								String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
										+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
										+ oppositeOrder.getAskingRate();

								String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
								
								log.info(matchMessage);
								
								log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

								kafkaProducerService.send(matchMessage, topic, null, null);


								iterator.remove();
								matchedOrders.add(oppositeOrder);

								List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

								if (orderList != null) {
									orderList.remove(newOrder);
									if (orderList.isEmpty()) {
										oppositeOrders.remove(newOrder.getAskingRate());
									}
								}
								sellOrders.remove(newOrder.getAskingRate());
								sellOrders.put(newOrder.getAskingRate(), matchedOrders);
								break;
							}
						}
						// case 4: both partial fill not allowed
						else if (!newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
							if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0) {
								// i.e match -> if buy amount equals with sell order buy amount

								String matchMessage =  "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
										+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
										+ oppositeOrder.getAskingRate();

								String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();
								
								log.info(matchMessage);
								
								log.info("Sending to " + "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

								kafkaProducerService.send(matchMessage, topic, null, null);


								iterator.remove();
								matchedOrders.add(oppositeOrder);

								List<NewOrder> orderList = oppositeOrders.get(newOrder.getAskingRate());

								if (orderList != null) {
									orderList.remove(newOrder);
									if (orderList.isEmpty()) {
										oppositeOrders.remove(newOrder.getAskingRate());
									}
								}
								sellOrders.remove(newOrder.getAskingRate());
								sellOrders.put(newOrder.getAskingRate(), matchedOrders);
								break;
							}

						}

					}

					if (oppositeOrderList.isEmpty()) {
						oppositeOrders.remove(price);
					}

				}

			}
			// }

		}
		for (NewOrder matchedOrder : matchedOrders) {
			oppositePrices.remove(matchedOrder.getAskingRate());
		}

	}

}
