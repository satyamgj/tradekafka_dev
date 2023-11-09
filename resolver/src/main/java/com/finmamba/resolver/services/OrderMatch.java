package com.finmamba.resolver.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.entities.transaction.MatchingTable;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;
import com.finmamba.resolver.repositories.MatchingRepository;
import com.finmamba.resolver.repositories.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderMatch {

	@Autowired
	CounterPartyTransactionRepository counterPartyTransactionRepository;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	MatchingRepository matchingRepository;

	@Autowired
	TransactionRepository transactionRepository;

	BigDecimal zero = new BigDecimal(0);

	public static final Long BUY = 1L;
	public static final Long SELL = 2L;

	private static final Long IN_REQUEST = 1L;
	private static final Long IN_NEGOTIATION = 5L;

	@Async
	public void initiateMatchingAlgorithm(MatchingTable order) {

		if (order.getBuyAmount().compareTo(zero) <= 0 || order.getSellAmount().compareTo(zero) <= 0) {
			return;
		}

		// Saving incoming request
		matchingRepository.save(order);

		if (order.getDirection() == 1) {

			List<MatchingTable> sellOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventTypeAndForTransactionId(
							order.getDestinationId(), SELL, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_NEGOTIATION, order.getTransactionId());

			List<MatchingTable> inRequestOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventType(
							order.getDestinationId(), SELL, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_REQUEST);

			matchOrders(Stream.concat(sellOrders.stream(), inRequestOrders.stream()).collect(Collectors.toList()),
					order);

		} else {

			List<MatchingTable> sellOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventTypeAndForTransactionId(
							order.getDestinationId(), BUY, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_NEGOTIATION, order.getTransactionId());

			List<MatchingTable> inRequestOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventType(
							order.getDestinationId(), BUY, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_REQUEST);

			matchOrders(Stream.concat(sellOrders.stream(), inRequestOrders.stream()).collect(Collectors.toList()),
					order);

		}
	}

	public List<CounterpartyTransactionDetails> matchRequest(MatchingTable order) {

		log.info("Received an individual order request for matching");

		if (order.getBuyAmount().compareTo(zero) <= 0 || order.getSellAmount().compareTo(zero) <= 0) {
			return Collections.emptyList();
		}

		if (order.getDirection() == 1) {

			List<MatchingTable> sellOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventTypeAndForTransactionId(
							order.getDestinationId(), SELL, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_NEGOTIATION, order.getTransactionId());

			List<MatchingTable> inRequestOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventType(
							order.getDestinationId(), SELL, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_REQUEST);

			return fetchMatchedOrders(
					Stream.concat(sellOrders.stream(), inRequestOrders.stream()).collect(Collectors.toList()), order);

		} else {

			List<MatchingTable> sellOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventTypeAndForTransactionId(
							order.getDestinationId(), BUY, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_NEGOTIATION, order.getTransactionId());

			List<MatchingTable> inRequestOrders = matchingRepository
					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyIdAndEventType(
							order.getDestinationId(), BUY, order.getBuyCurrencyId(), order.getSellCurrencyId(),
							IN_REQUEST);

			return fetchMatchedOrders(
					Stream.concat(sellOrders.stream(), inRequestOrders.stream()).collect(Collectors.toList()), order);

		}

//		if (order.getDirection() == 1) {
//			List<MatchingTable> sellOrders = matchingRepository
//					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyId(order.getDestinationId(), SELL,
//							order.getBuyCurrencyId(), order.getSellCurrencyId());
//			return fetchMatchedOrders(sellOrders, order);
//
//		} else {
//			List<MatchingTable> buyOrders = matchingRepository
//					.findByDestinationIdAndDirectionAndBuyCurrencyIdAndSellCurrencyId(order.getDestinationId(), BUY,
//							order.getBuyCurrencyId(), order.getSellCurrencyId());
//			return fetchMatchedOrders(buyOrders, order);
//		}

	}

	@Async
	public void matchOrders(List<MatchingTable> oppositeOrders, MatchingTable newOrder) {

		// List<BigDecimal> oppositePrices = new ArrayList<>(oppositeOrders.keySet());
//		List<NewOrder> matchedOrders = new ArrayList<>();

		CounterpartyTransactionDetails newOrderTransaction = counterPartyTransactionRepository
				.findById(newOrder.getOrderId()).get();

		for (MatchingTable oppositeOrder : oppositeOrders) {

			// List<NewOrder> oppositeOrderList = oppositeOrders.get(price);

			// Iterate over the list using an iterator to safely remove elements
			// Iterator<NewOrder> iterator = oppositeOrderList.iterator();

			// NewOrder oppositeOrder = iterator.next();

			// Check if new Order buy condition and asking rate comparison
			if (newOrder.getDirection() == 1 && (oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == -1
					|| oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 0)) {

				// Case 1: both isPartial true
				if (newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {

					CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
							.findById(oppositeOrder.getOrderId()).get();

					if (!oppositeOrderTransaction.isRequestActive() || oppositeOrderTransaction.getSourceId()
							.getId() == newOrderTransaction.getSourceId().getId()) {
						continue;
					}

					if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
							.getId() != newOrderTransaction.getSourceId().getId()) {

						String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
								+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
								+ oppositeOrder.getAskingRate();

						String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

						log.info(matchMessage);

						log.info("Sending to " + "realm_"
								+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

						kafkaProducerService.send(matchMessage, topic, null, null);
					}

				}
				// case 2: only buy order is partial allowed and not sell order not
				else if (newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0
							|| oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == -1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is greater than sell order amount

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();

						if (!oppositeOrderTransaction.isRequestActive() || oppositeOrderTransaction.getSourceId()
								.getId() == newOrderTransaction.getSourceId().getId()) {
							continue;
						}

						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {

							String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

							log.info(matchMessage);

							log.info("Sending to " + "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);

						}
					}

				}
				// case 3: only sell order is partial fill allowed.
				else if (!newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0
							|| oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is less than sell order amount

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();

						if (!oppositeOrderTransaction.isRequestActive() || oppositeOrderTransaction.getSourceId()
								.getId() == newOrderTransaction.getSourceId().getId()) {
							continue;
						}

						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {

							String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

							log.info(matchMessage);

							log.info("Sending to " + "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);
						}
					}
				}
				// case 4: both partial fill not allowed
				else if (!newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0) {
						// i.e match -> if buy amount equals with sell order buy amount

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();

						if (!oppositeOrderTransaction.isRequestActive() || oppositeOrderTransaction.getSourceId()
								.getId() == newOrderTransaction.getSourceId().getId()) {
							continue;
						}

						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {

							String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

							log.info(matchMessage);

							log.info("Sending to " + "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);

						}
					}

				}

			}
			// Check if new Order is a sell order
			else if (newOrder.getDirection() == 2
					&& (oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 1
							|| oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 0)) {

				// Case 1: both isPartial true
				if (newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
				}
				// case 2: only buy order is partial allowed and not sell order not
				else if (newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0
							|| oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == -1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is greater than sell order amount
						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();

						if (!oppositeOrderTransaction.isRequestActive() || oppositeOrderTransaction.getSourceId()
								.getId() == newOrderTransaction.getSourceId().getId()) {
							continue;
						}

						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {

							String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

							log.info(matchMessage);

							log.info("Sending to " + "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);
						}
					}

				}
				// case 3: only sell order is partial fill allowed.
				else if (!newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0
							|| oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is less than sell order amount

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();

						if (!oppositeOrderTransaction.isRequestActive() || oppositeOrderTransaction.getSourceId()
								.getId() == newOrderTransaction.getSourceId().getId()) {
							continue;
						}

						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {

							String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

							log.info(matchMessage);

							log.info("Sending to " + "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);
						}
					}
				}
				// case 4: both partial fill not allowed
				else if (!newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0) {
						// i.e match -> if buy amount equals with sell order buy amount

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();

						if (!oppositeOrderTransaction.isRequestActive() || oppositeOrderTransaction.getSourceId()
								.getId() == newOrderTransaction.getSourceId().getId()) {
							continue;
						}

						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {
							String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
									+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
									+ oppositeOrder.getAskingRate();

							String topic = "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

							log.info(matchMessage);

							log.info("Sending to " + "realm_"
									+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

							kafkaProducerService.send(matchMessage, topic, null, null);

						}
					}

				}

			}

		}

	}

	public List<CounterpartyTransactionDetails> fetchMatchedOrders(List<MatchingTable> oppositeOrders,
			MatchingTable newOrder) {

		CounterpartyTransactionDetails newOrderTransaction = counterPartyTransactionRepository
				.findById(newOrder.getOrderId()).get();

		List<CounterpartyTransactionDetails> matchedOrders = new ArrayList<>();

		for (MatchingTable oppositeOrder : oppositeOrders) {

			// Check if new Order buy condition and asking rate comparison

			if (newOrder.getDirection() == 1 && (oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == -1
					|| oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 0)) {

				// Case 1: both isPartial true
				if (newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {

					String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
							+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
							+ oppositeOrder.getAskingRate();

					String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

					log.info(matchMessage);

					log.info("Sending to " + "realm_"
							+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

					kafkaProducerService.send(matchMessage, topic, null, null);

					CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
							.findById(oppositeOrder.getOrderId()).get();
					if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
							.getId() != newOrderTransaction.getSourceId().getId()) {
						matchedOrders.add(oppositeOrderTransaction);
					}

				}
				// case 2: only buy order is partial allowed and not sell order not
				else if (newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0
							|| oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == -1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is greater than sell order amount

						String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
								+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
								+ oppositeOrder.getAskingRate();

						String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

						log.info(matchMessage);

						log.info("Sending to " + "realm_"
								+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

						kafkaProducerService.send(matchMessage, topic, null, null);

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();
						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {
							matchedOrders.add(oppositeOrderTransaction);
						}

					}

				}
				// case 3: only sell order is partial fill allowed.
				else if (!newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0
							|| oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is less than sell order amount

						String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
								+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
								+ oppositeOrder.getAskingRate();

						String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

						log.info(matchMessage);

						log.info("Sending to " + "realm_"
								+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

						kafkaProducerService.send(matchMessage, topic, null, null);

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();
						if (oppositeOrderTransaction.isRequestActive()) {
							matchedOrders.add(oppositeOrderTransaction);
						}

					}
				}
				// case 4: both partial fill not allowed
				else if (!newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getBuyAmount().compareTo(newOrder.getBuyAmount()) == 0) {
						// i.e match -> if buy amount equals with sell order buy amount

						String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
								+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
								+ oppositeOrder.getAskingRate();

						String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

						log.info(matchMessage);

						log.info("Sending to " + "realm_"
								+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

						kafkaProducerService.send(matchMessage, topic, null, null);

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();
						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {
							matchedOrders.add(oppositeOrderTransaction);
						}

					}

				}

			}
			// Check if new Order is a sell order
			else if (newOrder.getDirection() == 2
					&& (oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 1
							|| oppositeOrder.getAskingRate().compareTo(newOrder.getAskingRate()) == 0)) {

				// Case 1: both isPartial true
				if (newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {

					String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
							+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
							+ oppositeOrder.getAskingRate();

					String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

					log.info(matchMessage);

					log.info("Sending to " + "realm_"
							+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

					kafkaProducerService.send(matchMessage, topic, null, null);

					CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
							.findById(oppositeOrder.getOrderId()).get();
					if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
							.getId() != newOrderTransaction.getSourceId().getId()) {
						matchedOrders.add(oppositeOrderTransaction);
					}

				}
				// case 2: only buy order is partial allowed and not sell order not
				else if (newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0
							|| oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == -1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is greater than sell order amount

						String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
								+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
								+ oppositeOrder.getAskingRate();

						String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

						log.info(matchMessage);

						log.info("Sending to " + "realm_"
								+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

						kafkaProducerService.send(matchMessage, topic, null, null);

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();
						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {
							matchedOrders.add(oppositeOrderTransaction);
						}

					}

				}
				// case 3: only sell order is partial fill allowed.
				else if (!newOrder.getIsPartial() && oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0
							|| oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 1) {
						// i.e match -> if buy amount equals with sell order buy amount
						// & if buy amt is less than sell order amount

						String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
								+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
								+ oppositeOrder.getAskingRate();

						String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

						log.info(matchMessage);

						log.info("Sending to " + "realm_"
								+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

						kafkaProducerService.send(matchMessage, topic, null, null);

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();
						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {
							matchedOrders.add(oppositeOrderTransaction);
						}

					}
				}
				// case 4: both partial fill not allowed
				else if (!newOrder.getIsPartial() && !oppositeOrder.getIsPartial()) {
					if (oppositeOrder.getSellAmount().compareTo(newOrder.getSellAmount()) == 0) {
						// i.e match -> if buy amount equals with sell order buy amount

						String matchMessage = "Order matched buy Id: " + newOrder.getOrderId() + "@ price "
								+ newOrder.getAskingRate() + " with " + oppositeOrder.getOrderId() + " @ price "
								+ oppositeOrder.getAskingRate();

						String topic = "realm_" + newOrderTransaction.getDestinationId().getRealmName().toLowerCase();

						log.info(matchMessage);

						log.info("Sending to " + "realm_"
								+ newOrderTransaction.getDestinationId().getRealmName().toLowerCase());

						kafkaProducerService.send(matchMessage, topic, null, null);

						CounterpartyTransactionDetails oppositeOrderTransaction = counterPartyTransactionRepository
								.findById(oppositeOrder.getOrderId()).get();
						if (oppositeOrderTransaction.isRequestActive() && oppositeOrderTransaction.getSourceId()
								.getId() != newOrderTransaction.getSourceId().getId()) {
							matchedOrders.add(oppositeOrderTransaction);
						}

					}

				}

			}

		}

		return matchedOrders;

	}

}
