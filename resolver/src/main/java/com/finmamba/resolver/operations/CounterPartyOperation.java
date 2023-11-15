package com.finmamba.resolver.operations;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.configuration.EntityConfigurationSettings;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.BrokerToBrokerTransactionDetails;
import com.finmamba.entities.transaction.ConfirmedOrderDetails;
import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.entities.transaction.MatchingTable;
import com.finmamba.entities.transaction.TransactionBrokerDetails;
import com.finmamba.entities.transaction.TransactionDetail;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.CounterpartyTransactionDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SellSideTransactionDto;
import com.finmamba.models.transaction.ConfirmRequest;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.repositories.BrokerToBrokerRepository;
import com.finmamba.resolver.repositories.ConfirmationRepostory;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EntityConfigurationRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.QuoteRequestRepository;
import com.finmamba.resolver.repositories.TransactionBrokerDetailRepository;
import com.finmamba.resolver.repositories.TransactionRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.services.EventPublisher;
import com.finmamba.resolver.services.Matching;
import com.finmamba.resolver.services.OrderMatch;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.finmamba.resolver.util.ApplicationConstants;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CounterPartyOperation implements Operation {

	@Autowired
	QuoteRequestRepository quoteRequestRepository;

	@Autowired
	EventTypeRepository eventTypeRepository;

	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	TransactionServiceDal dal;

	@Autowired
	EventPublisher eventPublisher;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EntityConfigurationRepository entityConfigurationRepository;

	@Autowired
	CounterPartyTransactionRepository counterPartyTransactionRepository;

	@Autowired
	DirectionRepository directionRepository;

	@Autowired
	ConfirmationRepostory confirmationRepostory;

	@Autowired
	Matching matching;

	@Autowired
	OrderMatch orderMatch;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	TransactionBrokerDetailRepository transactionBrokerDetailRepository;

	@Autowired
	BrokerToBrokerRepository brokerToBrokerRepository;

	String spread = "0.0001"; // Default: HARDCODED

	String defaultConversionRate = "1";

	BigDecimal defaultValue = new BigDecimal("1");

	private static final String START = "start";

	private static final String END = "start";

	private static final Long IN_REQUEST = 1L;

	private static final Long IN_NEGOTIATION = 5L;
	
	private static final Long OUT_RESPONSE = 4L;
	
	private static final Long IN_REJECT = 7L;

	/****
	 * @apiNote : To dispatch quoteRequest to the brokers i.e IN_REQUEST for all
	 *          brokers
	 */

	@Override
	public Boolean dispatchRequestToBrokers(QuoteRequestDto quoteRequestDto) {

		String functionName = "dispatchRequestToBrokers";

		logTimeUpdate(functionName, START);

		try {

			TransactionDetail parent = transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId());

			long sourceId = quoteRequestDto.getSource().getId();

			List<InstitutionalEntity> listEligibleBrokers = dal.findEligibleBrokers(sourceId);

			// INSERTS IN_REQUEST FOR BROKERS

			listEligibleBrokers.forEach(broker -> {

				// Get Default of each broker

				logTimeUpdate(broker.getLongName(), START);

				EntityConfigurationSettings brokerConfiguration = entityConfigurationRepository.findByEntityId(broker);

				CounterpartyTransactionDetails counterPartyOperation = CounterpartyTransactionDetails.builder()

						.transactionId(parent)

						.buyCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId())
								.getBaseCurrency())

						.sellCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId())
								.getQuoteCurrency())

						.sourceId(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

						.destinationId(institutionalEntityRepository.getById(broker.getId()))

						.directionId(directionRepository.findById(quoteRequestDto.getBuySellDirection().getId()).get())

						.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId()))

						.bankBuyAmount(quoteRequestDto.getBuyAmount()).bankSellAmount(quoteRequestDto.getSellAmount())

						.clientOutright(quoteRequestDto.getDestinationOutright()).isRequestActive(true)

						.createdBy(userRepository.getById(quoteRequestDto.getCreatedBy().getId()))

						// .isPartial(quoteRequestDto.getIsPartial())

						.requestInitiatedBy(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

						.legInitiatedBy(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

						.viaBroker(false)

						.build();

				if (brokerConfiguration != null) {

					counterPartyOperation.setBrokerCommissionIsPip(brokerConfiguration.getIsPipOrAbsolute());

					if (brokerConfiguration.getIsPipOrAbsolute()) { // i.e pips

						counterPartyOperation.setBuySideSpread(brokerConfiguration.getDefaultSpreadBuy());

						counterPartyOperation.setSellSideSpread(brokerConfiguration.getDefaultSpreadSell());

						BigDecimal defaultSpread = new BigDecimal(spread); // Setting spread to default = 0.0001

						// calculating commission
						BigDecimal buyCommission = counterPartyOperation.getBankBuyAmount()
								.multiply(brokerConfiguration.getDefaultSpreadBuy()).multiply(defaultSpread);

						// calculating commission
						BigDecimal sellCommission = counterPartyOperation.getBankSellAmount()
								.multiply(brokerConfiguration.getDefaultSpreadBuy()).multiply(defaultSpread);

						counterPartyOperation.setCalculatedBuyCommission(buyCommission);

						counterPartyOperation.setCalculatedSellCommission(sellCommission);

						counterPartyOperation.setBuySideBrokerageCurrency(
								currencyRepository.getById(quoteRequestDto.getBaseCurrency().getId()));

						counterPartyOperation.setSellSideBrokerageCurrency(
								currencyRepository.getById(quoteRequestDto.getQuoteCurrency().getId()));

					} else {

						BigDecimal conversionRate = new BigDecimal(defaultConversionRate);

						counterPartyOperation.setBuySideConversionRate(conversionRate);

						counterPartyOperation.setSellSideConversionRate(conversionRate);

						counterPartyOperation.setBuySideSpread(brokerConfiguration.getBuySideAbsoluteCommission());

						counterPartyOperation.setSellSideSpread(brokerConfiguration.getSellSideAbsoluteCommission());

						counterPartyOperation
								.setBuySideBrokerageCurrency(brokerConfiguration.getBuyCommissionCurrency());

						counterPartyOperation
								.setSellSideBrokerageCurrency(brokerConfiguration.getSellCommissionCurrency());

						counterPartyOperation.setBuySideBaseAmount(brokerConfiguration.getBuySideBaseCommissionValue());

						counterPartyOperation
								.setSellSideBaseAmount(brokerConfiguration.getSellSideBaseCommissionValue());

						counterPartyOperation.setBuySideBrokerageBaseCurrency(
								brokerConfiguration.getBuySideBaseCommissionCurrency());

						counterPartyOperation.setSellSideBrokerageBaseCurrency(
								brokerConfiguration.getSellSideBaseCommissionCurrency());

						BigDecimal buyCommission = counterPartyOperation.getBankBuyAmount().multiply(conversionRate)
								.multiply(brokerConfiguration.getBuySideAbsoluteCommission())
								.divide(brokerConfiguration.getBuySideBaseCommissionValue());

						BigDecimal sellCommission = counterPartyOperation.getBankSellAmount().multiply(conversionRate)
								.multiply(brokerConfiguration.getSellSideAbsoluteCommission())
								.divide(brokerConfiguration.getSellSideBaseCommissionValue());

						counterPartyOperation.setCalculatedBuyCommission(buyCommission);

						counterPartyOperation.setCalculatedSellCommission(sellCommission);

					}

				} else {

					BigDecimal conversionRate = new BigDecimal(defaultConversionRate);

					counterPartyOperation.setBuySideConversionRate(conversionRate);

					counterPartyOperation.setSellSideConversionRate(conversionRate);

					counterPartyOperation.setBuySideSpread(defaultValue);

					counterPartyOperation.setSellSideSpread(defaultValue);

					counterPartyOperation.setBuySideBrokerageCurrency(currencyRepository.getById(1L));

					counterPartyOperation.setSellSideBrokerageCurrency(currencyRepository.getById(1L));

					counterPartyOperation.setBuySideBaseAmount(defaultValue);

					counterPartyOperation.setSellSideBaseAmount(defaultValue);

					counterPartyOperation.setBuySideBrokerageBaseCurrency(currencyRepository.getById(1L));

					counterPartyOperation.setSellSideBrokerageBaseCurrency(currencyRepository.getById(1L));

					BigDecimal buyCommission = counterPartyOperation.getBankBuyAmount().multiply(conversionRate)
							.multiply(defaultValue).divide(defaultValue);

					BigDecimal sellCommission = counterPartyOperation.getBankSellAmount().multiply(conversionRate)
							.multiply(defaultValue).divide(defaultValue);

					counterPartyOperation.setCalculatedBuyCommission(buyCommission);

					counterPartyOperation.setCalculatedSellCommission(sellCommission);

				}

				CounterpartyTransactionDetails transaction = counterPartyTransactionRepository
						.save(counterPartyOperation);

				// same order for bank

				CounterpartyTransactionDetails bankTransaction = CounterpartyTransactionDetails.builder()

						.transactionId(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()))

						.buyCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId())
								.getBaseCurrency())

						.sellCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId())
								.getQuoteCurrency())

						.sourceId(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

						.destinationId(institutionalEntityRepository.getById(broker.getId()))

						.directionId(directionRepository.findById(quoteRequestDto.getBuySellDirection().getId()).get())

						.eventType(eventTypeRepository.getById(ApplicationConstants.Event.OUT_REQUEST.getEventId()))

						.bankBuyAmount(quoteRequestDto.getBuyAmount()).bankSellAmount(quoteRequestDto.getSellAmount())

						.clientOutright(quoteRequestDto.getDestinationOutright()).isRequestActive(true)

						.createdBy(userRepository.getById(quoteRequestDto.getCreatedBy().getId()))

						// .isPartial(quoteRequestDto.getIsPartial())

						.requestInitiatedBy(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

						.build();

				if (brokerConfiguration != null) {

					bankTransaction.setBrokerCommissionIsPip(brokerConfiguration.getIsPipOrAbsolute());

					if (brokerConfiguration.getIsPipOrAbsolute()) { // i.e pips

						bankTransaction.setBuySideSpread(brokerConfiguration.getDefaultSpreadBuy());
						bankTransaction.setSellSideSpread(brokerConfiguration.getDefaultSpreadSell());

						BigDecimal defaultSpread = new BigDecimal(spread); // Setting spread to default = 0.0001

						// calculating commission
						BigDecimal buyCommission = bankTransaction.getBankBuyAmount()
								.multiply(brokerConfiguration.getDefaultSpreadBuy()).multiply(defaultSpread);

						// calculating commission
						BigDecimal sellCommission = bankTransaction.getBankSellAmount()
								.multiply(brokerConfiguration.getDefaultSpreadBuy()).multiply(defaultSpread);

						bankTransaction.setCalculatedBuyCommission(buyCommission);

						bankTransaction.setCalculatedSellCommission(sellCommission);

						bankTransaction.setBuySideBrokerageCurrency(
								currencyRepository.getById(quoteRequestDto.getBaseCurrency().getId()));

						bankTransaction.setSellSideBrokerageCurrency(
								currencyRepository.getById(quoteRequestDto.getQuoteCurrency().getId()));

					} else {

						BigDecimal conversionRate = new BigDecimal(defaultConversionRate);

						bankTransaction.setBuySideConversionRate(conversionRate);

						bankTransaction.setSellSideConversionRate(conversionRate);

						bankTransaction.setBuySideSpread(brokerConfiguration.getBuySideAbsoluteCommission());

						bankTransaction.setSellSideSpread(brokerConfiguration.getSellSideAbsoluteCommission());

						bankTransaction.setBuySideBrokerageCurrency(brokerConfiguration.getBuyCommissionCurrency());

						bankTransaction.setSellSideBrokerageCurrency(brokerConfiguration.getSellCommissionCurrency());

						bankTransaction.setBuySideBaseAmount(brokerConfiguration.getBuySideBaseCommissionValue());

						bankTransaction.setSellSideBaseAmount(brokerConfiguration.getSellSideBaseCommissionValue());

						bankTransaction.setBuySideBrokerageBaseCurrency(
								brokerConfiguration.getBuySideBaseCommissionCurrency());

						bankTransaction.setSellSideBrokerageBaseCurrency(
								brokerConfiguration.getSellSideBaseCommissionCurrency());

						BigDecimal buyCommission = bankTransaction.getBankBuyAmount().multiply(conversionRate)
								.multiply(brokerConfiguration.getBuySideAbsoluteCommission())
								.divide(brokerConfiguration.getBuySideBaseCommissionValue());

						BigDecimal sellCommission = bankTransaction.getBankSellAmount().multiply(conversionRate)
								.multiply(brokerConfiguration.getSellSideAbsoluteCommission())
								.divide(brokerConfiguration.getSellSideBaseCommissionValue());

						bankTransaction.setCalculatedBuyCommission(buyCommission);

						bankTransaction.setCalculatedSellCommission(sellCommission);

					}

				} else {

					bankTransaction.setBrokerCommissionIsPip(false); // Default to false i.e absolute

					BigDecimal conversionRate = new BigDecimal(defaultConversionRate);

					bankTransaction.setBuySideConversionRate(conversionRate);

					bankTransaction.setSellSideConversionRate(conversionRate);

					bankTransaction.setBuySideSpread(defaultValue);

					bankTransaction.setSellSideSpread(defaultValue);

					bankTransaction.setBuySideBrokerageCurrency(currencyRepository.getById(1L));

					bankTransaction.setSellSideBrokerageCurrency(currencyRepository.getById(1L));

					bankTransaction.setBuySideBaseAmount(defaultValue);

					bankTransaction.setSellSideBaseAmount(defaultValue);

					bankTransaction.setBuySideBrokerageBaseCurrency(currencyRepository.getById(1L));

					bankTransaction.setSellSideBrokerageBaseCurrency(currencyRepository.getById(1L));

					BigDecimal buyCommission = bankTransaction.getBankBuyAmount().multiply(conversionRate)
							.multiply(defaultValue).divide(defaultValue);

					BigDecimal sellCommission = bankTransaction.getBankSellAmount().multiply(conversionRate)
							.multiply(defaultValue).divide(defaultValue);

					bankTransaction.setCalculatedBuyCommission(buyCommission);

					bankTransaction.setCalculatedSellCommission(sellCommission);

				}

				counterPartyTransactionRepository.save(bankTransaction);

				// initiate Matching algo
//				NewOrder newOrder = NewOrder.builder().askingRate(counterPartyOperation.getClientOutright())
//									.orderId(transaction.getId())
//									.isPartial(counterPartyOperation.getTransactionId().getIsPartial())
//									.buyAmount(counterPartyOperation.getBankBuyAmount())
//									.sellAmount(counterPartyOperation.getBankSellAmount())
//									.direction(modelMapper.map(counterPartyOperation.getDirectionId(), DirectionDto.class))
//									.buyCurrency(modelMapper.map(counterPartyOperation.getTransactionId().getBaseCurrency(), CurrencyDto.class))
//									.sellCurrency(modelMapper.map(counterPartyOperation.getTransactionId().getQuoteCurrency(), CurrencyDto.class))
//									.insertionTime(new Timestamp(System.currentTimeMillis()))
//									.build();
//				
//				matching.initiateMatchingAlgorithm(newOrder);

				// Since we are matching only broker orders

				MatchingTable matchingTable = MatchingTable.builder()
						.askingRate(counterPartyOperation.getClientOutright()).destinationId(broker.getId())
						.sourceId(sourceId).isPartial(quoteRequestDto.getIsPartial()).orderId(transaction.getId())
						.buyAmount(counterPartyOperation.getBankBuyAmount())
						.sellAmount(counterPartyOperation.getBankSellAmount())
						.direction(counterPartyOperation.getDirectionId().getId())
						.buyCurrencyId(counterPartyOperation.getTransactionId().getBaseCurrency().getId())
						.sellCurrencyId(counterPartyOperation.getTransactionId().getQuoteCurrency().getId())
						.eventType(IN_REQUEST)
						.transactionId(
								transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()).getId())
						.forTransactionId(
								transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()).getId())
						.insertionTime(new Timestamp(System.currentTimeMillis())).build();

				orderMatch.initiateMatchingAlgorithm(matchingTable);

				// Publish on kafka topic
				String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "."
						+ broker.getClientRoleName() + "." + broker.getGroupName();

				CounterpartyTransactionDto counterpartyTransactionDto = modelMapper.map(transaction,
						CounterpartyTransactionDto.class);

				log.info("Pushing on kafka topic: " + kafkaTopic);

				List<CounterpartyTransactionDto> listSavedQuote = new ArrayList<>();

				listSavedQuote.add(counterpartyTransactionDto);

				kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);

				log.info("adding to transaction broker details");

				TransactionBrokerDetails newRequest = TransactionBrokerDetails.builder().brokerId(broker.getId())
						.transactionId(parent).directionId(parent.getBuySellDirection()).isActive(true)
						.lastTransactionAt(Instant.now()).build();

				transactionBrokerDetailRepository.save(newRequest);

				logTimeUpdate(broker.getLongName(), END);

			});

			logTimeUpdate(functionName, END);

			return Boolean.TRUE;

		} catch (Exception e) {

			logException(functionName, e.getMessage());

			return Boolean.FALSE;

		}

	}

	private void logTimeUpdate(String functionName, String status) {
		log.info(functionName + " " + status + " " + Instant.now());
	}

	private void logException(String functionName, String exceptionMessage) {
		log.info("Exception occurred " + functionName + " " + exceptionMessage);
	}

	/****
	 * @apiNote : To dispatch quoteRequest to the counterparties i.e OUT_REQUEST for
	 *          all BROKERS and IN_REQUEST for Counterparties.
	 * 
	 */
	@Override
	public Boolean dispatchRequestToCounterparties(TransactionRequest brokerDispatchRequest, Long sourceEntity) {

		logTimeUpdate("dispatchRequestToCounterparties", START);

//		InstitutionalEntity broker = institutionalEntityRepository.findById(brokerDispatchRequest.getSource()).get();
//
//		InstitutionalEntity source = institutionalEntityRepository.findById(sourceEntity).get();

		/*
		 * NO NEED TO FALSE ANY TRANSACTION
		 * 
		 * List<CounterpartyTransactionDetails> prevTransactions =
		 * counterPartyTransactionRepository
		 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
		 * 
		 * source ,broker , true
		 * ,transactionRepository.findById(brokerDispatchRequest.getTransactionId()).get
		 * () );
		 * 
		 * for(CounterpartyTransactionDetails transactions:prevTransactions) { if
		 * (transactions != null) { transactions.setRequestActive(false); } else {
		 * log.info("No previous transaction found!"); }
		 * 
		 * counterPartyTransactionRepository.save(transactions); }
		 */

		try {

			List<InstitutionalEntity> listEligibleCounterparties = dal.findEligibleCounterparties(
					brokerDispatchRequest.getSource(), brokerDispatchRequest.getSendToBrokers());

			listEligibleCounterparties.forEach(counterparty -> {

				if (sourceEntity != counterparty.getId()) { // Condition that order should not be circulated to the
															// entity itself

					log.info("Sending order -> " + counterparty.getLongName());

					// Setting Destination as this counterparty
					brokerDispatchRequest.setDestination(counterparty.getId());

					CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
							ApplicationConstants.Event.OUT_REQUEST.getEventId(), brokerDispatchRequest);
					// Saving w.r.t broker perspective

					if (brokerDispatchRequest.getSendToBrokers()) {
						transactionForBroker.setViaBroker(true);
					} else {
						transactionForBroker.setViaBroker(false);
					}

					counterPartyTransactionRepository.save(transactionForBroker);

					// Setting IN_REQUEST for counterparty
					CounterpartyTransactionDetails transactionForCounterparty = addToCounterPartyTransactionTable(
							ApplicationConstants.Event.IN_REQUEST.getEventId(), brokerDispatchRequest);

					if (brokerDispatchRequest.getSendToBrokers()) {
						transactionForCounterparty.setViaBroker(true);
					} else {
						transactionForCounterparty.setViaBroker(false);
					}

					counterPartyTransactionRepository.save(transactionForCounterparty);

					if (brokerDispatchRequest.getSendToBrokers()) {

						log.info("Sending to broker ->");
						BrokerToBrokerTransactionDetails brokerToBrokerTransactionDetails = BrokerToBrokerTransactionDetails
								.builder().sourceBrokerId(brokerDispatchRequest.getSource()).brokerId(counterparty.getId())
								.directionId(directionRepository.findById(brokerDispatchRequest.getDirection()).get())
								.isActive(true).lastTransactionAt(Instant.now())
								.transactionId(
										transactionRepository.findById(brokerDispatchRequest.getTransactionId()).get())
								.cpTransactionId(transactionForCounterparty)
								.statusId(IN_REQUEST)
								.build();

						brokerToBrokerRepository.save(brokerToBrokerTransactionDetails);
					}

					// Publish on kafka topic
					String kafkaTopic = counterparty.getRealmName() + "." + counterparty.getClientName() + "."
							+ counterparty.getClientRoleName() + "." + counterparty.getGroupName();

					log.info("Pushing on kafka topic: " + kafkaTopic);

					SellSideTransactionDto newRequest = modelMapper.map(transactionForCounterparty,
							SellSideTransactionDto.class);

					List<SellSideTransactionDto> listNewQuote = new ArrayList<>();
					listNewQuote.add(newRequest);

					kafkaProducerService.send(listNewQuote, kafkaTopic, null, null);
				}

			});

			logTimeUpdate("dispatchRequestToCounterparties", END);

			return true;

		} catch (Exception e) {

			e.printStackTrace();
			logException("dispatchRequestToCounterparties", e.getMessage());

			logTimeUpdate("dispatchRequestToCounterparties", END);

			return false;
		}

	}

	public CounterpartyTransactionDetails addToCounterPartyTransactionTable(Long eventId,
			TransactionRequest counterpartyTransactionDetails) {

		TransactionDetail transaction = transactionRepository
				.findById(counterpartyTransactionDetails.getTransactionId()).get();

		CounterpartyTransactionDetails newTransactionDetails = CounterpartyTransactionDetails.builder()

				.directionId(directionRepository.getById(counterpartyTransactionDetails.getDirection()))

				.transactionId(transaction)

				.buyCcy(transaction.getBaseCurrency())

				.sellCcy(transaction.getQuoteCurrency())

				.sourceId(institutionalEntityRepository.getById(counterpartyTransactionDetails.getSource()))

				.destinationId(institutionalEntityRepository.getById(counterpartyTransactionDetails.getDestination()))

				.eventType(eventTypeRepository.getById(eventId)).isRequestActive(true)

				.buySideSpread(counterpartyTransactionDetails.getBuySideSpread())

				.sellSideSpread(counterpartyTransactionDetails.getSellSideSpread())

				.brokerCommissionIsPip(counterpartyTransactionDetails.getBrokerCommissionIsPip())

				.bankBuyAmount(counterpartyTransactionDetails.getBuyAmount())

				.bankSellAmount(counterpartyTransactionDetails.getSellAmount())

				.buySideBaseAmount(counterpartyTransactionDetails.getBuySideBaseAmount())

				.buySideBrokerageBaseCurrency(
						currencyRepository.getById(counterpartyTransactionDetails.getBuySideBrokerageBaseCurrency()))

				.buySideBrokerageCurrency(
						currencyRepository.getById(counterpartyTransactionDetails.getBuySideBrokerageCurrency()))

				.buySideConversionRate(counterpartyTransactionDetails.getBuySideConversionRate())

				.sellSideBaseAmount(counterpartyTransactionDetails.getSellSideBaseAmount())

				.sellSideBrokerageBaseCurrency(
						currencyRepository.getById(counterpartyTransactionDetails.getSellSideBrokerageBaseCurrency()))

				.sellSideBrokerageCurrency(
						currencyRepository.getById(counterpartyTransactionDetails.getSellSideBrokerageCurrency())

				).sellSideConversionRate(counterpartyTransactionDetails.getSellSideConversionRate())

				.clientOutright(counterpartyTransactionDetails.getDestinationOutright())

				// .isPartial(counterpartyTransactionDetails.getIsPartial())

				.legInitiatedBy(
						institutionalEntityRepository.getById(counterpartyTransactionDetails.getLegInitiatedBy()))

				.createdBy(userRepository.getById(counterpartyTransactionDetails.getCreatedBy()))

				.build();

		if (eventId == ApplicationConstants.Event.IN_REQUEST.getEventId()
				|| eventId == ApplicationConstants.Event.OUT_REQUEST.getEventId()) {
			newTransactionDetails.setResponseInitiatedBy(null);

		} else {
			if (counterpartyTransactionDetails.getSource() == transaction.getSource().getId()) {
				log.info("Its a buyer entry ");
				log.info("hence not modifying response intiatedBy");
				newTransactionDetails.setResponseInitiatedBy(counterPartyTransactionRepository
						.getById(counterpartyTransactionDetails.getCounterpartyDetailId()).getResponseInitiatedBy());
			} else {
				if (institutionalEntityRepository.getById(counterpartyTransactionDetails.getSource()).getEntityType()
						.getId() == 2L) {
					log.info("Broker nego");
					newTransactionDetails.setResponseInitiatedBy(counterPartyTransactionRepository
							.getById(counterpartyTransactionDetails.getCounterpartyDetailId())
							.getResponseInitiatedBy());
				} else {
					newTransactionDetails.setResponseInitiatedBy(
							institutionalEntityRepository.getById(counterpartyTransactionDetails.getSource()));
				}
			}
		}

		return newTransactionDetails;

	}

	@Override
	public void insertOutResponse(TransactionRequest request) {

		logTimeUpdate("insertOutResponse", START);

		try {

			InstitutionalEntity responseInitiatedBy = null;

			InstitutionalEntity source = institutionalEntityRepository.getById(request.getSource());

			log.info("Transaction triggered by Bank. Hence deactivating banks incoming request");

			List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
					.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

							institutionalEntityRepository.getById(request.getDestination()), source, true,
							transactionRepository.findById(request.getTransactionId()).get());

			for (CounterpartyTransactionDetails transactions : prevTransactions) {
				if (transactions != null) {

					transactions.setRequestActive(false);
					counterPartyTransactionRepository.save(transactions);
				} else {
					log.info("Not out coming request from bank. Seems to be on brokers side.");
				}

			}

			TransactionDetail parentTransaction = transactionRepository.findById(request.getTransactionId()).get();

			responseInitiatedBy = counterPartyTransactionRepository.findById(request.getCounterpartyDetailId()).get()
					.getResponseInitiatedBy();

			if (source.getId() == parentTransaction.getSource().getId()) {

				log.info("Confirmation initiated by Buyer");
				log.info("Activating the negotiated party request");

				List<CounterpartyTransactionDetails> prevNegotiationTransactions = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(responseInitiatedBy,
								institutionalEntityRepository.getById(request.getDestination()), false,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevNegotiationTransactions) {
					if (transactions != null) {
						transactions.setRequestActive(true);
						counterPartyTransactionRepository.save(transactions);
					} else {
						log.info("Not out coming request from bank. Seems to be on brokers side.");
					}

				}

			}

			CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.OUT_RESPONSE.getEventId(), request);
			counterPartyTransactionRepository.save(transactionForBank);

			CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.IN_RESPONSE.getEventId(), request);
			counterPartyTransactionRepository.save(transactionForBroker);

			InstitutionalEntity broker = institutionalEntityRepository.getById(request.getDestination());

			// Updating transactionbrokerdetail
			TransactionBrokerDetails brokerDetails = transactionBrokerDetailRepository.findByTransactionIdAndBrokerId(
					transactionRepository.findById(request.getTransactionId()).get(), request.getDestination());

			brokerDetails.setLastTransactionAt(Instant.now());
			transactionBrokerDetailRepository.save(brokerDetails);

			// Publish on kafka topic
			String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "." + broker.getClientRoleName()
					+ "." + broker.getGroupName();

			log.info("Pushing on kafka topic: " + kafkaTopic);

			CounterpartyTransactionDto inResponse = modelMapper.map(transactionForBroker,
					CounterpartyTransactionDto.class);

			List<CounterpartyTransactionDto> inResponseList = new ArrayList<>();
			inResponseList.add(inResponse);

			kafkaProducerService.send(inResponseList, kafkaTopic, null, null);

			MatchingTable matchingTable = MatchingTable.builder().askingRate(transactionForBroker.getClientOutright())
					.destinationId(transactionForBroker.getDestinationId().getId())
					.sourceId(transactionForBroker.getSourceId().getId())
					.isPartial(transactionForBroker.getTransactionId().getIsPartial())
					.orderId(transactionForBroker.getId()).buyAmount(transactionForBroker.getBankBuyAmount())
					.sellAmount(transactionForBroker.getBankSellAmount())
					.direction(transactionForBroker.getDirectionId().getId())
					.buyCurrencyId(transactionForBroker.getTransactionId().getBaseCurrency().getId())
					.sellCurrencyId(transactionForBroker.getTransactionId().getQuoteCurrency().getId())
					.eventType(IN_NEGOTIATION).transactionId(request.getTransactionId())
					.transactionId(request.getTransactionId())
					.forTransactionId(request.getTransactionId())
					.insertionTime(new Timestamp(System.currentTimeMillis()))
					
					.build();

			orderMatch.initiateMatchingAlgorithm(matchingTable);

		} catch (Exception e) {
			logException("insertOutResponse", e.getLocalizedMessage());
		}

		logTimeUpdate("insertOutResponse", END);

	}

	@Override
	public void insertOutNegotiation(TransactionRequest request) {

		logTimeUpdate("insertOutNegotiation", START);

		try {

			InstitutionalEntity responseInitiatedBy = null;

			/*
			 * if source is initiator
			 */
			if (institutionalEntityRepository.getById(request.getSource()).getId() == transactionRepository
					.findById(request.getTransactionId()).get().getSource().getId()) {

				log.info("Negotiation initiated by buySide");
				log.info("activate negotiation from sell-side party");
				log.info("Getting sell Side from key ResponseIntitiatedBy");

				log.info("De-activating latest quote sent to the broker");

				List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionIdOrderByIdDesc(

								institutionalEntityRepository.getById(request.getSource()),
								institutionalEntityRepository.getById(request.getDestination()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevTransactions) {
					if (transactions != null) {
						transactions.setRequestActive(false);
//						responseInitiatedBy = transactions.getResponseInitiatedBy(); //Setting response Initiated by
					} else {
						log.info("No previous transaction found!");
					}

					counterPartyTransactionRepository.save(transactions);
				}

				// Deactivating brokers request to source
				List<CounterpartyTransactionDetails> prevBrokerToSourceTransactions = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevBrokerToSourceTransactions) {
					if (transactions != null) {
						transactions.setRequestActive(false);
//						responseInitiatedBy = transactions.getResponseInitiatedBy(); //Setting response Initiated by
					} else {
						log.info("No previous transaction found!");
					}

					counterPartyTransactionRepository.save(transactions);
				}

				responseInitiatedBy = counterPartyTransactionRepository.getById(request.getCounterpartyDetailId())
						.getResponseInitiatedBy();

				List<CounterpartyTransactionDetails> prevNegotiationTransaction = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(responseInitiatedBy,
								institutionalEntityRepository.getById(request.getDestination()), false,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevNegotiationTransaction) {
					if (transactions != null) {

						if (transactions.getEventType().getId() == IN_NEGOTIATION) {

							TransactionRequest newRequest = convert(transactions);

							CounterpartyTransactionDetails newCounterpartyTransactionDetails = addToCounterPartyTransactionTable(
									IN_NEGOTIATION, newRequest);
							
						//	newCounterpartyTransactionDetails.setSourceId(institutionalEntityRepository.getById(request.getSource()));
						
							newCounterpartyTransactionDetails.setDirectionId(directionRepository.findById(request.getDirection()).get());
							newCounterpartyTransactionDetails.setBankBuyAmount(request.getBuyAmount());
							newCounterpartyTransactionDetails.setBankSellAmount(request.getSellAmount());
	
							newCounterpartyTransactionDetails.setClientOutright(request.getDestinationOutright());

							counterPartyTransactionRepository.save(newCounterpartyTransactionDetails);

							break;
						}

					} else {
						log.info("No previous transaction found!");
					}

				}

				// Updating transactionbrokerdetail
				TransactionBrokerDetails brokerDetails = transactionBrokerDetailRepository
						.findByTransactionIdAndBrokerId(
								transactionRepository.findById(request.getTransactionId()).get(),
								request.getDestination());

				brokerDetails.setLastTransactionAt(Instant.now());
				transactionBrokerDetailRepository.save(brokerDetails);

			} else if (institutionalEntityRepository.getById(request.getSource()).getEntityType().getId() == 1L) {
				List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevTransactions) {
					if (transactions != null) {
						transactions.setRequestActive(false);
					} else {
						log.info("No previous transaction found!");
					}

					counterPartyTransactionRepository.save(transactions);
				}

				// Updating transactionbrokerdetail
				TransactionBrokerDetails brokerDetails = transactionBrokerDetailRepository
						.findByTransactionIdAndBrokerId(
								transactionRepository.findById(request.getTransactionId()).get(),
								request.getDestination());

				brokerDetails.setLastTransactionAt(Instant.now());
				transactionBrokerDetailRepository.save(brokerDetails);

			}

			else if (institutionalEntityRepository.getById(request.getSource()).getEntityType().getId() == 2L) {

				log.info("Negotiation initiated by broker");
				log.info("De-activate negotiation from sell-side party");
				log.info("Getting sell Side from key ResponseIntitiatedBy");

				CounterpartyTransactionDetails responseTo = counterPartyTransactionRepository
						.findById(request.getCounterpartyDetailId()).get();

				responseInitiatedBy = responseTo.getResponseInitiatedBy();

				List<CounterpartyTransactionDetails> prevNegotiationTransaction = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(responseInitiatedBy,
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevNegotiationTransaction) {
					if (transactions != null) {
						// if Destination is buyer
						// Deactivate in_nego only
						if (institutionalEntityRepository.getById(request.getDestination())
								.getId() == transactionRepository.findById(request.getTransactionId()).get().getSource()
										.getId()) {
							if (transactions.getEventType().getId() == IN_NEGOTIATION) {
								transactions.setRequestActive(false);
								counterPartyTransactionRepository.save(transactions);

							}
						} else {
							// if destination is seller make both in_nego/out nego deactive
							transactions.setRequestActive(false);
							counterPartyTransactionRepository.save(transactions);

						}
					} else {
						log.info("No previous transaction found!");
					}

				}

				// Updating transactionbrokerdetail
				TransactionBrokerDetails brokerDetails = transactionBrokerDetailRepository
						.findByTransactionIdAndBrokerId(
								transactionRepository.findById(request.getTransactionId()).get(), request.getSource());

				brokerDetails.setLastTransactionAt(Instant.now());
				transactionBrokerDetailRepository.save(brokerDetails);

			} else if (institutionalEntityRepository.getById(request.getDestination()).getEntityType().getId() == 2L) {

				log.info("Sending negotiation to a broker");

				log.info("deactivating previous sent requests");

				List<CounterpartyTransactionDetails> prevTransaction = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
								institutionalEntityRepository.getById(request.getSource()),
								institutionalEntityRepository.getById(request.getDestination()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transaction : prevTransaction) {
					transaction.setRequestActive(false);
					counterPartyTransactionRepository.save(transaction);

				}

			}

			log.info("setting previous incoming request of bank as false");

			log.info("Updating the transactionc");

			CounterpartyTransactionDetails negotiatedTransactionForBank = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.OUT_NEGOTIATION.getEventId(), request);
			counterPartyTransactionRepository.save(negotiatedTransactionForBank);

			CounterpartyTransactionDetails negotiatedTransactionForBroker = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.IN_NEGOTIATION.getEventId(), request);
			counterPartyTransactionRepository.save(negotiatedTransactionForBroker);

			InstitutionalEntity broker = institutionalEntityRepository.findById(request.getDestination()).get();

			/*
			 * Since Nego is point to point no need to run match algorithm
			 */
			MatchingTable matchingTable = MatchingTable.builder()
					.askingRate(negotiatedTransactionForBroker.getClientOutright())
					.destinationId(negotiatedTransactionForBroker.getDestinationId().getId())
					.sourceId(negotiatedTransactionForBroker.getSourceId().getId())
					.isPartial(negotiatedTransactionForBroker.getTransactionId().getIsPartial())
					.orderId(negotiatedTransactionForBroker.getId())
					.buyAmount(negotiatedTransactionForBroker.getBankBuyAmount())
					.sellAmount(negotiatedTransactionForBroker.getBankSellAmount())
					.direction(negotiatedTransactionForBroker.getDirectionId().getId())
					.buyCurrencyId(negotiatedTransactionForBroker.getTransactionId().getBaseCurrency().getId())
					.sellCurrencyId(negotiatedTransactionForBroker.getTransactionId().getQuoteCurrency().getId())
					.eventType(IN_NEGOTIATION).transactionId(request.getTransactionId())
					.forTransactionId(request.getTransactionId()) // Always negotiation will happen for single
																	// transactionId
					.insertionTime(new Timestamp(System.currentTimeMillis())).build();
			
			orderMatch.initiateMatchingAlgorithm(matchingTable);

			if (institutionalEntityRepository.getById(request.getDestination()).getEntityType().getId() == 2L
					&& 
					institutionalEntityRepository.getById(request.getSource()).getEntityType().getId() == 2L
					) {
				log.info("Updating broker to broker transaction");
				BrokerToBrokerTransactionDetails brokerToBrokerTransaction = brokerToBrokerRepository
						.findByBrokerIdAndSourceBrokerIdAndTransactionId(request.getSource(), request.getDestination(),
								transactionRepository.findById(request.getTransactionId()).get());
				brokerToBrokerTransaction.setCpTransactionId(negotiatedTransactionForBroker);
				brokerToBrokerTransaction.setLastTransactionAt(Instant.now());
				brokerToBrokerRepository.save(brokerToBrokerTransaction);
				matchingTable.setEventType(IN_REQUEST);
			}

			// Publish on kafka topic
			String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "." + broker.getClientRoleName()
					+ "." + broker.getGroupName();

			log.info("Pushing on kafka topic: " + kafkaTopic);

			CounterpartyTransactionDto inResponse = modelMapper.map(negotiatedTransactionForBroker,
					CounterpartyTransactionDto.class);

			List<CounterpartyTransactionDto> inResponseList = new ArrayList<>();
			inResponseList.add(inResponse);

			kafkaProducerService.send(inResponseList, kafkaTopic, null, null);

			// Since we are matching only broker orders

			orderMatch.initiateMatchingAlgorithm(matchingTable);

		} catch (Exception e) {
			logException("insertOutNegotiation", e.getLocalizedMessage());
		}

		logTimeUpdate("insertOutNegotiation", END);

	}

	@Override
	public void insertOutReject(TransactionRequest request) {

		logTimeUpdate("insertOutReject", START);

		try {
			/*
			 * NO NEED TO SET FALSE ANY TRANSACTION
			 * 
			 * List<CounterpartyTransactionDetails> prevTransactions =
			 * counterPartyTransactionRepository
			 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
			 * 
			 * institutionalEntityRepository.getById(request.getDestination())
			 * ,institutionalEntityRepository.getById(request.getSource()) , true
			 * ,transactionRepository.findById(request.getTransactionId()).get() );
			 * 
			 * for(CounterpartyTransactionDetails transactions:prevTransactions) { if
			 * (transactions != null) { transactions.setRequestActive(false); } else {
			 * log.info("No previous transaction found!"); }
			 * 
			 * counterPartyTransactionRepository.save(transactions); }
			 * 
			 */

			InstitutionalEntity source = institutionalEntityRepository.getById(request.getSource());

			// If Rejected by the initiator
			// Keep the last active quote with the broker

			if (institutionalEntityRepository.getById(request.getSource()).getId() == transactionRepository
					.findById(request.getTransactionId()).get().getSource().getId()) {

				log.info("Reject sent by the buyer hence not deactivating previous active request to the broker");

				log.info("Re-activating response sent by selling counterparty");

				InstitutionalEntity responseInitiatedBy = counterPartyTransactionRepository
						.getById(request.getCounterpartyDetailId()).getResponseInitiatedBy();

				List<CounterpartyTransactionDetails> prevNegotiationTransaction = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(responseInitiatedBy,
								institutionalEntityRepository.getById(request.getDestination()), false,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevNegotiationTransaction) {
					if (transactions != null) {

						if (transactions.getEventType().getId() == IN_NEGOTIATION) {

							TransactionRequest newRequest = convert(transactions);

							CounterpartyTransactionDetails newCounterpartyTransactionDetails = addToCounterPartyTransactionTable(
									IN_NEGOTIATION, newRequest);

							counterPartyTransactionRepository.save(newCounterpartyTransactionDetails);

						}

					} else {
						log.info("No previous transaction found!");
					}

				}

			}

			else if (source.getEntityType().getId() == 2L) {

				/*
				 * log.
				 * info("Transaction triggered by Broker. Hence deactivating banks incoming request"
				 * );
				 * 
				 * List<CounterpartyTransactionDetails> prevTransactions =
				 * counterPartyTransactionRepository
				 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
				 * 
				 * institutionalEntityRepository.getById(request.getDestination()),
				 * institutionalEntityRepository.getById(request.getSource()), true,
				 * transactionRepository.findById(request.getTransactionId()).get());
				 * 
				 * for (CounterpartyTransactionDetails transactions : prevTransactions) { if
				 * (transactions != null && (transactions.getEventType().getId() == 2L ||
				 * transactions.getEventType().getId() == 4L ||
				 * transactions.getEventType().getId() == 6L ||
				 * transactions.getEventType().getId() == 8L)) {
				 * 
				 * transactions.setRequestActive(false);
				 * counterPartyTransactionRepository.save(transactions); } else {
				 * log.info("Not out coming request from bank. Seems to be on brokers side."); }
				 * 
				 * }
				 * 
				 * List<CounterpartyTransactionDetails> prevBankRequestTransactions =
				 * counterPartyTransactionRepository
				 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
				 * 
				 * institutionalEntityRepository.getById(request.getDestination()),
				 * institutionalEntityRepository.getById(request.getSource()), true,
				 * transactionRepository.findById(request.getTransactionId()).get());
				 * 
				 * for (CounterpartyTransactionDetails transactions :
				 * prevBankRequestTransactions) { if (transactions != null) {
				 * 
				 * transactions.setRequestActive(false);
				 * counterPartyTransactionRepository.save(transactions); } else {
				 * log.info("Not out coming request from bank. Seems to be on brokers side."); }
				 * 
				 * }
				 */
				log.info("Reject not allowed from broker entity for bank transactions");
				log.info("It seems its rejecting broker to broker request");
				
				
				BrokerToBrokerTransactionDetails brokerTransactionDetails = brokerToBrokerRepository
						.findByBrokerIdAndSourceBrokerIdAndTransactionId(request.getDestination(), request.getSource()
								,transactionRepository.findById(request.getTransactionId()).get()
								);
				
				if(brokerTransactionDetails == null) {
					log.info("Seems you have sent incorrect transactionID");
				}
				else {
					brokerTransactionDetails.setStatusId(IN_REJECT);
					brokerToBrokerRepository.save(brokerTransactionDetails);
					
				}
				//Also Deactivating the broker broker In-response requests
				List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								counterPartyTransactionRepository.findById(request.getCounterpartyDetailId()).get().getTransactionId())
								;

				for (CounterpartyTransactionDetails transactions : prevTransactions) {
					if (transactions != null) {

						transactions.setRequestActive(false);
						counterPartyTransactionRepository.save(transactions);
					} else {
						log.info("Not out coming request from bank. Seems to be on brokers side.");
					}

				}
				
				
			} else {

				log.info("Transaction triggered by Bank. Hence deactivating banks incoming request");

				List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevTransactions) {
					if (transactions != null) {

						transactions.setRequestActive(false);
						counterPartyTransactionRepository.save(transactions);
					} else {
						log.info("Not out coming request from bank. Seems to be on brokers side.");
					}

				}
			}

			// Updating transactionbrokerdetail
			TransactionBrokerDetails brokerDetails = transactionBrokerDetailRepository.findByTransactionIdAndBrokerId(
					transactionRepository.findById(request.getTransactionId()).get(), request.getDestination());

			brokerDetails.setLastTransactionAt(Instant.now());
			transactionBrokerDetailRepository.save(brokerDetails);

			CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.OUT_REJECT.getEventId(), request);
			counterPartyTransactionRepository.save(transactionForBank);

			CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.IN_REJECT.getEventId(), request);

			counterPartyTransactionRepository.save(transactionForBroker);

			InstitutionalEntity broker = institutionalEntityRepository.getById(request.getDestination());

			// Publish on kafka topic
			String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "." + broker.getClientRoleName()
					+ "." + broker.getGroupName();

			log.info("Pushing on kafka topic: " + kafkaTopic);

			CounterpartyTransactionDto inResponse = modelMapper.map(transactionForBroker,
					CounterpartyTransactionDto.class);

			List<CounterpartyTransactionDto> inResponseList = new ArrayList<>();
			inResponseList.add(inResponse);

			kafkaProducerService.send(inResponseList, kafkaTopic, null, null);

		} catch (Exception e) {
			logException("insertOutNegotiation", e.getLocalizedMessage());
		}

		logTimeUpdate("insertOutNegotiation", END);
	}

	@Override
	public Boolean confirmRequest(ConfirmRequest confirmRequest) {

		logTimeUpdate("confirmRequest", START);

		try {

			InstitutionalEntity buySideCounterparty = institutionalEntityRepository
					.findById(confirmRequest.getBuySideCounterParty()).get(); // BuySide Counterparty

			InstitutionalEntity sellSideCounterparty = institutionalEntityRepository
					.findById(confirmRequest.getSellSideCounterPart()).get(); // SellSide Counterparty

			InstitutionalEntity broker = institutionalEntityRepository.findById(confirmRequest.getBrokerId()).get(); // Middle
																														// Party

			CounterpartyTransactionDetails buySideCounterPartyTransaction = counterPartyTransactionRepository
					.findById(confirmRequest.getBuySideCounterPartyTransactionId()).get(); // BuySide Counterparty
																							// details

			CounterpartyTransactionDetails sellSideCounterPartyTransaction = counterPartyTransactionRepository
					.findById(confirmRequest.getSellSideCounterPartyTransactionId()).get(); // SellSide Counterparty
																							// details

			TransactionDetail sellTransaction = transactionRepository
					.getById(sellSideCounterPartyTransaction.getTransactionId().getId()); // Sell Side origin
																							// transaction

			TransactionDetail buyTransaction = transactionRepository
					.getById(buySideCounterPartyTransaction.getTransactionId().getId()); // Buy Side origin transaction

			if (buySideCounterparty.getEntityType().getId() == 2L) // Checking if buyside is broker
			{
				log.info("It seems broker has accepted a broker order");
				log.info("Sending in-response to initial broker");

				// deactivating previous transaction for broker
//				List<BrokerToBrokerTransactionDetails>transactions = brokerToBrokerRepository.find

				CounterpartyTransactionDetails transactionBuyBroker = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.OUT_RESPONSE.getEventId(), convert(sellSideCounterPartyTransaction));

				transactionBuyBroker.setSourceId(sellSideCounterPartyTransaction.getDestinationId());// sellBroker
				transactionBuyBroker.setDestinationId(buySideCounterPartyTransaction.getSourceId()); // original broker
				transactionBuyBroker.setViaBroker(true);

				counterPartyTransactionRepository.save(transactionBuyBroker);

				CounterpartyTransactionDetails transactionSellBroker = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.IN_RESPONSE.getEventId(), convert(sellSideCounterPartyTransaction));

				transactionSellBroker.setSourceId(sellSideCounterPartyTransaction.getDestinationId());
				transactionSellBroker.setDestinationId(buySideCounterPartyTransaction.getSourceId());
				transactionSellBroker.setViaBroker(true);

				counterPartyTransactionRepository.save(transactionSellBroker);

				InstitutionalEntity destination = buySideCounterPartyTransaction.getSourceId();

				// Publish on kafka topic
				String kafkaTopic = destination.getRealmName() + "." + destination.getClientName() + "."
						+ destination.getClientRoleName() + "." + destination.getGroupName();

				log.info("Pushing on kafka topic: " + kafkaTopic);

				CounterpartyTransactionDto inResponse = modelMapper.map(transactionBuyBroker,
						CounterpartyTransactionDto.class);

				List<CounterpartyTransactionDto> inResponseList = new ArrayList<>();
				inResponseList.add(inResponse);

				kafkaProducerService.send(inResponseList, kafkaTopic, null, null);

				MatchingTable matchingTable = MatchingTable.builder()
						.askingRate(transactionBuyBroker.getClientOutright())
						.destinationId(buySideCounterPartyTransaction.getSourceId().getId())
						.sourceId(sellSideCounterPartyTransaction.getDestinationId().getId())
						.isPartial(transactionBuyBroker.getTransactionId().getIsPartial())
						.orderId(transactionBuyBroker.getId()).buyAmount(transactionBuyBroker.getBankBuyAmount())
						.sellAmount(transactionBuyBroker.getBankSellAmount())
						.direction(transactionBuyBroker.getDirectionId().getId())
						.buyCurrencyId(transactionBuyBroker.getTransactionId().getBaseCurrency().getId())
						.sellCurrencyId(transactionBuyBroker.getTransactionId().getQuoteCurrency().getId())
						.eventType(IN_NEGOTIATION).transactionId(transactionBuyBroker.getTransactionId().getId())
						.forTransactionId(buyTransaction.getId())
						.insertionTime(new Timestamp(System.currentTimeMillis())).build();

				orderMatch.initiateMatchingAlgorithm(matchingTable);
				
				log.info("Changing status of broker to broker request to out response");
				BrokerToBrokerTransactionDetails brokerToBrokerTransactionDetails = brokerToBrokerRepository
						.findByBrokerIdAndSourceBrokerIdAndTransactionId(sellSideCounterPartyTransaction.getDestinationId().getId(), buySideCounterPartyTransaction.getSourceId().getId(), buyTransaction);
				
				//Deactivating broker to broker request
				brokerToBrokerTransactionDetails.setStatusId(OUT_RESPONSE);
				brokerToBrokerRepository.save(brokerToBrokerTransactionDetails);

				log.info("de-activating the entries sent by buyside broker");

				List<CounterpartyTransactionDetails> orders = counterPartyTransactionRepository
						.findAllByDestinationIdOrSourceIdAndIsRequestActiveAndTransactionId(
								sellSideCounterPartyTransaction.getDestinationId(),
								buySideCounterPartyTransaction.getSourceId(), true, buyTransaction);

				if (orders.size() > 0) {
					for (CounterpartyTransactionDetails order : orders) {
						
					
							order.setRequestActive(false);
							counterPartyTransactionRepository.save(order);
						
					}
				}

			} else if (sellSideCounterparty.getEntityType().getId() == 2L) {

				log.info("It seems buy side broker has accepted order with a sell side broker");
				log.info("completing orders ...");

				sellTransaction.setIsConfirmed(true);
				transactionRepository.save(sellTransaction);

				buyTransaction.setIsConfirmed(true);
				transactionRepository.save(buyTransaction);

				// Saving confirmed orders
				ConfirmedOrderDetails buyConfirm = ConfirmedOrderDetails.builder().brokerId(broker)
						.buySideCounterpartyTransactionId(buySideCounterPartyTransaction)
						.sellSideCounterpartyTransactionId(sellSideCounterPartyTransaction)
						.sellSideId(sellSideCounterparty).buySideId(buySideCounterparty).transactionId(buyTransaction)
						.createdBy(userRepository.findById(confirmRequest.getCreatedBy()).get()).build();

				confirmationRepostory.save(buyConfirm);
				
				//As broker is a seller for 2nd broker
				//broker will source of sellSideCounterpartyTransaction
				//buySide initiator of sell Side

				ConfirmedOrderDetails sellConfirm = ConfirmedOrderDetails.builder()
						.brokerId(sellSideCounterPartyTransaction.getSourceId())
						.buySideCounterpartyTransactionId(buySideCounterPartyTransaction)
						.sellSideCounterpartyTransactionId(sellSideCounterPartyTransaction)
						.sellSideId(broker).buySideId(sellSideCounterPartyTransaction.getLegInitiatedBy()).transactionId(sellTransaction)
						.createdBy(userRepository.findById(confirmRequest.getCreatedBy()).get()).build();
				confirmationRepostory.save(sellConfirm);

				// Deactivating buy-bank-broker

				List<CounterpartyTransactionDetails> buySideTransactionActive = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
								buySideCounterPartyTransaction.getSourceId(), broker, true, buyTransaction);

				for (CounterpartyTransactionDetails buySideTransaction : buySideTransactionActive) {
					if (buySideTransaction != null) {
						buySideTransaction.setRequestActive(false);
						counterPartyTransactionRepository.save(buySideTransaction);
					}

				}

				// deactivating broker-broker
				List<CounterpartyTransactionDetails> sellSideTransactionActive = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
								sellSideCounterPartyTransaction.getSourceId(), broker, true, sellTransaction);

				for (CounterpartyTransactionDetails sellSideTransaction : sellSideTransactionActive) {
					if (sellSideTransaction != null) {
						sellSideTransaction.setRequestActive(false);
						counterPartyTransactionRepository.save(sellSideTransaction);
					}

				}

				log.info("Deactivating broker to broker request");
				BrokerToBrokerTransactionDetails brokerToBrokerTransactionDetails = brokerToBrokerRepository
						.findByBrokerIdAndSourceBrokerIdAndTransactionId(sellSideCounterPartyTransaction.getSourceId().getId(), broker.getId(), buyTransaction);
				
				//Deactivating broker to broker request
				brokerToBrokerTransactionDetails.setActive(false);
				brokerToBrokerRepository.save(brokerToBrokerTransactionDetails);
				
				log.info("Broker-to-broker confirmation completed.");

			} else {
				if (buySideCounterparty.getId() == buyTransaction.getSource().getId()) { // transaction

					buyTransaction.setIsConfirmed(true);
					transactionRepository.save(buyTransaction);
					
					ConfirmedOrderDetails buyConfirm = ConfirmedOrderDetails.builder().brokerId(broker)
							.buySideCounterpartyTransactionId(buySideCounterPartyTransaction)
							.sellSideCounterpartyTransactionId(sellSideCounterPartyTransaction)
							.sellSideId(sellSideCounterparty).buySideId(buySideCounterparty).transactionId(buyTransaction)
							.createdBy(userRepository.findById(confirmRequest.getCreatedBy()).get()).build();

					confirmationRepostory.save(buyConfirm);

				}

				if (sellSideCounterparty.getId() == sellTransaction.getSource().getId()) { // transaction

					sellTransaction.setIsConfirmed(true);
					transactionRepository.save(sellTransaction);
					
					ConfirmedOrderDetails sellConfirm = ConfirmedOrderDetails.builder().brokerId(broker)
							.buySideCounterpartyTransactionId(buySideCounterPartyTransaction)
							.sellSideCounterpartyTransactionId(sellSideCounterPartyTransaction)
							.sellSideId(sellSideCounterparty).buySideId(buySideCounterparty).transactionId(sellTransaction)
							.createdBy(userRepository.findById(confirmRequest.getCreatedBy()).get()).build();
					confirmationRepostory.save(sellConfirm);

				}

				/*
				 * if (buySideCounterparty.getId() != buyTransaction.getSource().getId() &&
				 * sellSideCounterparty.getId() != sellTransaction.getSource().getId() &&
				 * buySideCounterparty.getEntityType().getId() == 1L &&
				 * sellSideCounterparty.getEntityType().getId() == 1L) { // Add new Transaction
				 * to Db
				 * 
				 * TransactionDetail newBuyTransaction = TransactionDetail.builder()
				 * .baseCurrency(buySideCounterPartyTransaction.getTransactionId().
				 * getBaseCurrency())
				 * .quoteCurrency(buySideCounterPartyTransaction.getTransactionId().
				 * getQuoteCurrency())
				 * .buyAmount(buySideCounterPartyTransaction.getBankBuyAmount())
				 * .sellAmount(buySideCounterPartyTransaction.getBankSellAmount())
				 * .buySellDirection(buySideCounterPartyTransaction.getDirectionId())
				 * .createdBy(buySideCounterPartyTransaction.getCreatedBy())
				 * .destinationOutright(buySideCounterPartyTransaction.getClientOutright()).
				 * isConfirmed(true)
				 * .isPartial(buySideCounterPartyTransaction.getTransactionId().getIsPartial()).
				 * build();
				 * 
				 * TransactionDetail newSellTransaction = TransactionDetail.builder()
				 * .baseCurrency(sellSideCounterPartyTransaction.getTransactionId().
				 * getBaseCurrency())
				 * .quoteCurrency(sellSideCounterPartyTransaction.getTransactionId().
				 * getQuoteCurrency())
				 * .buyAmount(sellSideCounterPartyTransaction.getBankBuyAmount())
				 * .sellAmount(sellSideCounterPartyTransaction.getBankSellAmount())
				 * .buySellDirection(sellSideCounterPartyTransaction.getDirectionId())
				 * .createdBy(sellSideCounterPartyTransaction.getCreatedBy())
				 * .destinationOutright(sellSideCounterPartyTransaction.getClientOutright()).
				 * isConfirmed(true)
				 * .isPartial(sellSideCounterPartyTransaction.getTransactionId().getIsPartial())
				 * .build();
				 * 
				 * transactionRepository.save(newBuyTransaction);
				 * transactionRepository.save(newSellTransaction);
				 * 
				 * }
				 */





				// Deactivating counterPartyBroker-Broker transaction

				List<CounterpartyTransactionDetails> buySideTransactionActive = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
								buySideCounterPartyTransaction.getSourceId(), broker, true, buyTransaction);

				for (CounterpartyTransactionDetails buySideTransaction : buySideTransactionActive) {
					if (buySideTransaction != null) {
						buySideTransaction.setRequestActive(false);
						counterPartyTransactionRepository.save(buySideTransaction);
					}

				}

				List<CounterpartyTransactionDetails> sellSideTransactionActive = counterPartyTransactionRepository
						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
								sellSideCounterPartyTransaction.getSourceId(), broker, true, sellTransaction);

				for (CounterpartyTransactionDetails sellSideTransaction : sellSideTransactionActive) {
					if (sellSideTransaction != null) {
						sellSideTransaction.setRequestActive(false);
						counterPartyTransactionRepository.save(sellSideTransaction);
					}

				}
			}

			/*
			 * if (buySideCounterparty.getEntityType().getId() == 2L) {
			 * 
			 * // Set everytime request initiated if Bank // Set everytime same
			 * requestInitiatedBy while broker sends to broker
			 * 
			 * List<CounterpartyTransactionDetails> buytransaction =
			 * counterPartyTransactionRepository
			 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
			 * buySideCounterPartyTransaction.getLegInitiatedBy(), buySideCounterparty,
			 * true, buyTransaction);
			 * 
			 * List<CounterpartyTransactionDetails> selltransaction =
			 * counterPartyTransactionRepository
			 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(broker,
			 * buySideCounterparty, true, buyTransaction);
			 * 
			 * for (CounterpartyTransactionDetails transaction : buytransaction) { if
			 * (transaction != null) { transaction.setRequestActive(false);
			 * counterPartyTransactionRepository.save(transaction); } }
			 * 
			 * for (CounterpartyTransactionDetails transaction : selltransaction) { if
			 * (transaction != null) { transaction.setRequestActive(false);
			 * counterPartyTransactionRepository.save(transaction); } }
			 * 
			 * ConfirmRequest newRequest =
			 * ConfirmRequest.builder().brokerId(buySideCounterparty.getId())
			 * .buySideCounterParty(buySideCounterPartyTransaction.getLegInitiatedBy().getId
			 * ()) .sellSideCounterPart(broker.getId())
			 * .buySideCounterPartyTransactionId(buytransaction.get(0).getId())
			 * .sellSideCounterPartyTransactionId(selltransaction.get(0).getId()).build();
			 * 
			 * confirmRequest(newRequest);
			 * 
			 * } if (sellSideCounterparty.getEntityType().getId() == 2L) {
			 * 
			 * // Set everytime request initiated if Bank // Set everytime same
			 * requestInitiatedBy while broker sends to broker
			 * 
			 * List<CounterpartyTransactionDetails> buytransaction =
			 * counterPartyTransactionRepository
			 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
			 * sellSideCounterPartyTransaction.getLegInitiatedBy(), sellSideCounterparty,
			 * true, sellTransaction);
			 * 
			 * List<CounterpartyTransactionDetails> selltransaction =
			 * counterPartyTransactionRepository
			 * .findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(broker,
			 * sellSideCounterparty, true, sellTransaction);
			 * 
			 * for (CounterpartyTransactionDetails transaction : buytransaction) { if
			 * (transaction != null) { transaction.setRequestActive(false);
			 * counterPartyTransactionRepository.save(transaction); } }
			 * 
			 * for (CounterpartyTransactionDetails transaction : selltransaction) { if
			 * (transaction != null) { transaction.setRequestActive(false);
			 * counterPartyTransactionRepository.save(transaction); } }
			 * 
			 * ConfirmRequest newRequest =
			 * ConfirmRequest.builder().brokerId(buySideCounterparty.getId())
			 * .buySideCounterParty(buySideCounterPartyTransaction.getLegInitiatedBy().getId
			 * ()) .sellSideCounterPart(broker.getId())
			 * .buySideCounterPartyTransactionId(buytransaction.get(0).getId())
			 * .sellSideCounterPartyTransactionId(selltransaction.get(0).getId()).build();
			 * 
			 * confirmRequest(newRequest);
			 * 
			 * }
			 */

//			//Fetching buySide bank : FE can get using requestInitiated By
//			InstitutionalEntity buySide = institutionalEntityRepository.getById(confirmRequest.getBuySideBank());
//			
//			//Fetching Broker : i.e the logged in realm
//			InstitutionalEntity broker = institutionalEntityRepository.getById(confirmRequest.getBrokerId());
//			
//			//Fetching buySide bank : FE can get using requestInitiated By
//
//			InstitutionalEntity sellSide = institutionalEntityRepository.getById(confirmRequest.getSellSideBank());
//			CounterpartyTransactionDetails buySideCPTradeDetails = counterPartyTransactionRepository
//					.findById(confirmRequest.getBuySideCounterPartyTransactionId()).get();
//			CounterpartyTransactionDetails sellSideCPTradeDetails = counterPartyTransactionRepository
//					.findById(confirmRequest.getSellSideCounterPartyTransactionId()).get();
//
//			TransactionDetail sellTransaction = transactionRepository
//					.getById(sellSideCPTradeDetails.getTransactionId().getId());
//
//			TransactionDetail buyTransaction = transactionRepository
//					.getById(buySideCPTradeDetails.getTransactionId().getId());
//
//			List<CounterpartyTransactionDetails> sellSideTransactionActive = counterPartyTransactionRepository
//					.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(sellSideCPTradeDetails.getSourceId(), broker, true,
//							sellTransaction);
//
//			for (CounterpartyTransactionDetails sellSideTransaction : sellSideTransactionActive) {
//				sellSideTransaction.setRequestActive(false);
//				counterPartyTransactionRepository.save(sellSideTransaction);
//			}
//
//			List<CounterpartyTransactionDetails> buySideTransactionActive = counterPartyTransactionRepository
//					.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(buySideCPTradeDetails.getSourceId(), broker, true,
//							buyTransaction);
//
//			for (CounterpartyTransactionDetails buySideTransaction : buySideTransactionActive) {
//				buySideTransaction.setRequestActive(false);
//				counterPartyTransactionRepository.save(buySideTransaction);
//			}
//
//			if (buySideCPTradeDetails.getSourceId().getId() == buySideCPTradeDetails.getTransactionId().getSource()
//					.getId()) {
//
//				log.info("Buy Side counterparty transaction completed ");
//				
//				buyTransaction.setIsConfirmed(true);
//				transactionRepository.save(buyTransaction);
//
//				ConfirmedOrderDetails request = ConfirmedOrderDetails.builder().brokerId(broker).buySideId(buySide)
//						.sellSideId(sellSide).buySideCounterpartyTransactionId(buySideCPTradeDetails)
//						.sellSideCounterpartyTransactionId(sellSideCPTradeDetails).transactionId(buyTransaction)
//						.createdBy(userRepository.getById(confirmRequest.getCreatedBy())).build();
//
//				confirmationRepostory.save(request);
//
//			}else if(buySideCPTradeDetails.getSourceId().getEntityType().getId() == 1L) {
//				
//				log.info("BuySide Counterpaerty details source is a bank");
//				buyTransaction.setIsConfirmed(true);
//				transactionRepository.save(buyTransaction);
//
//				ConfirmedOrderDetails request = ConfirmedOrderDetails.builder().brokerId(broker).buySideId(buySide)
//						.sellSideId(sellSide).buySideCounterpartyTransactionId(buySideCPTradeDetails)
//						.sellSideCounterpartyTransactionId(sellSideCPTradeDetails).transactionId(buyTransaction)
//						.createdBy(userRepository.getById(confirmRequest.getCreatedBy())).build();
//
//				confirmationRepostory.save(request);
//				
//				
//			}else if(buySideCPTradeDetails.getSourceId().getEntityType().getId() == 2L) {
//				
//				log.info("BuySide Counterpaerty details source is a broker");
//				buyTransaction.setIsConfirmed(true);
//				transactionRepository.save(buyTransaction);
//
//				InstitutionalEntity requestInitiatedBy = institutionalEntityRepository.findById(buySideCPTradeDetails.getRequestInitiatedBy().getId()).get();
//				
//				ConfirmedOrderDetails request = ConfirmedOrderDetails.builder().brokerId(buySideCPTradeDetails.getSourceId())
//						.buySideId(requestInitiatedBy)
//						.sellSideId(broker)
//						.buySideCounterpartyTransactionId(buySideCPTradeDetails)
//						.sellSideCounterpartyTransactionId(sellSideCPTradeDetails).transactionId(buyTransaction)
//						.createdBy(userRepository.getById(confirmRequest.getCreatedBy())).build();
//
//				confirmationRepostory.save(request);
//				
//				log.info("Sending a notification to bank as well from requestedBy");
//				
//				log.info("Bank is " + buySideCPTradeDetails.getRequestInitiatedBy().getLongName());
//				
//				
//				List<CounterpartyTransactionDetails> requestActive = counterPartyTransactionRepository
//						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(requestInitiatedBy,
//								buySideCPTradeDetails.getSourceId(), true,
//								buyTransaction);
//				
//				for(CounterpartyTransactionDetails transaction:requestActive) {
//					transaction.setRequestActive(false);
//					counterPartyTransactionRepository.save(transaction);
//				}
//			}
//			
//
//			if (sellSideCPTradeDetails.getSourceId().getId() == sellSideCPTradeDetails.getTransactionId().getSource()
//					.getId()) {
//
//				log.info("Sell Side counterparty transaction completed ");
//				
//				sellTransaction.setIsConfirmed(true);
//				transactionRepository.save(sellTransaction);
//
//				ConfirmedOrderDetails request = ConfirmedOrderDetails.builder().brokerId(broker).buySideId(buySide)
//						.sellSideId(sellSide).buySideCounterpartyTransactionId(buySideCPTradeDetails)
//						.sellSideCounterpartyTransactionId(sellSideCPTradeDetails).transactionId(sellTransaction)
//						.createdBy(userRepository.getById(confirmRequest.getCreatedBy())).build();
//
//				confirmationRepostory.save(request);
//
//			}
//				
//			else if (sellSideCPTradeDetails.getSourceId().getEntityType().getId() == 1L) {
//
//				log.info("Sell Side counterparty transaction is by bank ");
//				
//				sellTransaction.setIsConfirmed(true);
//				transactionRepository.save(sellTransaction);
//
//				ConfirmedOrderDetails request = ConfirmedOrderDetails.builder().brokerId(broker).buySideId(buySide)
//						.sellSideId(sellSide).buySideCounterpartyTransactionId(buySideCPTradeDetails)
//						.sellSideCounterpartyTransactionId(sellSideCPTradeDetails).transactionId(sellTransaction)
//						.createdBy(userRepository.getById(confirmRequest.getCreatedBy())).build();
//
//				confirmationRepostory.save(request);
//				
//
//			}
//	
//			else if (sellSideCPTradeDetails.getSourceId().getEntityType().getId() == 2L) {
//
//				log.info("Sell Side counterparty transaction completed ");
//				
//				sellTransaction.setIsConfirmed(true);
//				transactionRepository.save(sellTransaction);
//
//				InstitutionalEntity requestInitiatedBy = institutionalEntityRepository.findById(sellSideCPTradeDetails.getRequestInitiatedBy().getId()).get();
//				
//				ConfirmedOrderDetails request = ConfirmedOrderDetails.builder().brokerId(sellSideCPTradeDetails.getSourceId())
//						.buySideId(requestInitiatedBy)
//						.sellSideId(broker)
//						.buySideCounterpartyTransactionId(buySideCPTradeDetails)
//						.sellSideCounterpartyTransactionId(sellSideCPTradeDetails).transactionId(sellTransaction)
//						.createdBy(userRepository.getById(confirmRequest.getCreatedBy())).build();
//
//				confirmationRepostory.save(request);
//				
//				log.info("Sending a notification to bank as well from requestedBy");
//				
//				log.info("Bank is " + sellSideCPTradeDetails.getRequestInitiatedBy().getLongName());
//				
//				
//				List<CounterpartyTransactionDetails> requestActive = counterPartyTransactionRepository
//						.findBySourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(requestInitiatedBy,
//								sellSideCPTradeDetails.getSourceId(), true,
//								sellTransaction);
//				
//				for(CounterpartyTransactionDetails transaction:requestActive) {
//					transaction.setRequestActive(false);
//					counterPartyTransactionRepository.save(transaction);
//				}
//
//			}

			logTimeUpdate("confirmRequest", END);

			return Boolean.TRUE;

		} catch (Exception e) {
			// TODO: handle exception
			logException("confirmRequest", e.getLocalizedMessage());

			logTimeUpdate("confirmRequest", END);

			return Boolean.FALSE;

		}

	}

	@Override
	public Boolean confirmRecursiveBrokerRequest(ConfirmRequest confirmRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	private TransactionRequest convert(CounterpartyTransactionDetails counterpartyTransactionDetails) {

		TransactionRequest request = TransactionRequest.builder()
				.counterpartyDetailId(counterpartyTransactionDetails.getId())
				.brokerCommissionIsPip(counterpartyTransactionDetails.getBrokerCommissionIsPip())
				.buyAmount(counterpartyTransactionDetails.getBankBuyAmount())
				.buySideSpread(counterpartyTransactionDetails.getBuySideSpread())
				.buySideBaseAmount(counterpartyTransactionDetails.getBuySideBaseAmount())
				.buySideBrokerageBaseCurrency(counterpartyTransactionDetails.getBuySideBrokerageBaseCurrency().getId())
				.buySideBrokerageCurrency(counterpartyTransactionDetails.getBuySideBrokerageCurrency().getId())
				.buySideConversionRate(counterpartyTransactionDetails.getBuySideConversionRate())
				.calculatedBuyCommission(counterpartyTransactionDetails.getCalculatedBuyCommission())
				.calculatedSellCommission(counterpartyTransactionDetails.getCalculatedSellCommission())
				.createdBy(counterpartyTransactionDetails.getCreatedBy().getId())
				.destination(counterpartyTransactionDetails.getDestinationId().getId())
				.destinationOutright(counterpartyTransactionDetails.getClientOutright())
				.direction(counterpartyTransactionDetails.getDirectionId().getId())
				.legInitiatedBy(counterpartyTransactionDetails.getLegInitiatedBy().getId())
				.transactionId(counterpartyTransactionDetails.getTransactionId().getId())
				.sourceOutright(counterpartyTransactionDetails.getTransactionId().getSourceOutright())
				.source(counterpartyTransactionDetails.getSourceId().getId())
				.sendToBrokers(counterpartyTransactionDetails.getViaBroker())
				.sellSideSpread(counterpartyTransactionDetails.getSellSideSpread())
				.sellSideConversionRate(counterpartyTransactionDetails.getSellSideConversionRate())
				.sellSideBrokerageCurrency(counterpartyTransactionDetails.getSellSideBrokerageCurrency().getId())
				.sellSideBrokerageBaseCurrency(
						counterpartyTransactionDetails.getSellSideBrokerageBaseCurrency().getId())
				.sellSideBaseAmount(counterpartyTransactionDetails.getSellSideBaseAmount())
				.sellAmount(counterpartyTransactionDetails.getBankSellAmount())
				.sendToBrokers(counterpartyTransactionDetails.getViaBroker()).build();
		return request;
	}

}
