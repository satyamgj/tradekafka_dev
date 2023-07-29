package com.finmamba.resolver.operations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.configuration.EntityConfigurationSettings;
import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.staticdata.Product;
import com.finmamba.entities.staticdata.Tenor;
import com.finmamba.entities.transaction.ConfirmedOrderDetails;
import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.entities.transaction.Customer;
import com.finmamba.entities.transaction.MatchingTable;
import com.finmamba.entities.transaction.TransactionDetail;
import com.finmamba.entities.transaction.User;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.CounterpartyTransactionDto;
import com.finmamba.models.dto.CurrencyDto;
import com.finmamba.models.dto.DirectionDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SellSideTransactionDto;
import com.finmamba.models.matching.NewOrder;
import com.finmamba.models.transaction.ConfirmRequest;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.exception.EntityNotFound;
import com.finmamba.resolver.repositories.ConfirmationRepostory;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EntityConfigurationRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.QuoteRequestRepository;
import com.finmamba.resolver.repositories.TransactionRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.services.EventPublisher;
import com.finmamba.resolver.services.Matching;
import com.finmamba.resolver.services.OrderMatch;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.finmamba.resolver.util.ApplicationConstants;
import com.finmamba.resolver.util.UniqueIdentifierGenerator;

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

	String spread = "0.0001"; // Default: HARDCODED

	String defaultConversionRate = "1";

	BigDecimal defaultValue = new BigDecimal("1");

	private static final String START = "start";

	private static final String END = "start";

	/****
	 * @apiNote : To dispatch quoteRequest to the brokers i.e IN_REQUEST for all
	 *          brokers
	 */

	@Override
	public Boolean dispatchRequestToBrokers(QuoteRequestDto quoteRequestDto) {

		String functionName = "dispatchRequestToBrokers";

		logTimeUpdate(functionName, START);

		try {

			long sourceId = quoteRequestDto.getSource().getId();

			List<InstitutionalEntity> listEligibleBrokers = dal.findEligibleBrokers(sourceId);

			// INSERTS IN_REQUEST FOR BROKERS

			listEligibleBrokers.forEach(broker -> {

				// Get Default of each broker

				logTimeUpdate(broker.getLongName(), START);

				EntityConfigurationSettings brokerConfiguration = entityConfigurationRepository.findByEntityId(broker);

				CounterpartyTransactionDetails counterPartyOperation = CounterpartyTransactionDetails.builder()

						.transactionId(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()))
						
						.buyCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()).getBaseCurrency())
						
						.sellCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()).getQuoteCurrency())

						.sourceId(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

						.destinationId(institutionalEntityRepository.getById(broker.getId()))

						.directionId(directionRepository.findById(quoteRequestDto.getBuySellDirection().getId()).get())

						.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId()))

						.bankBuyAmount(quoteRequestDto.getBuyAmount()).bankSellAmount(quoteRequestDto.getSellAmount())

						.clientOutright(quoteRequestDto.getDestinationOutright()).isRequestActive(true)

						.createdBy(userRepository.getById(quoteRequestDto.getCreatedBy().getId()))

						// .isPartial(quoteRequestDto.getIsPartial())

						.requestInitiatedBy(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

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

					counterPartyOperation.setBrokerCommissionIsPip(false); // Default to false i.e absolute

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
				
				//same order for bank
				
				CounterpartyTransactionDetails bankTransaction = CounterpartyTransactionDetails.builder()

						.transactionId(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()))
						
						.buyCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()).getBaseCurrency())
						
						.sellCcy(transactionRepository.findByTransactionId(quoteRequestDto.getTransactionId()).getQuoteCurrency())

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

						bankTransaction
								.setBuySideBrokerageCurrency(brokerConfiguration.getBuyCommissionCurrency());

						bankTransaction
								.setSellSideBrokerageCurrency(brokerConfiguration.getSellCommissionCurrency());

						bankTransaction.setBuySideBaseAmount(brokerConfiguration.getBuySideBaseCommissionValue());

						bankTransaction
								.setSellSideBaseAmount(brokerConfiguration.getSellSideBaseCommissionValue());

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
				
				//initiate Matching algo
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
									
					//Since we are matching only broker orders
					
					MatchingTable matchingTable = MatchingTable.builder()
							.askingRate(counterPartyOperation.getClientOutright())
							.destinationId(broker.getId())
							.sourceId(sourceId)
							.isPartial(quoteRequestDto.getIsPartial())
							.orderId(transaction.getId())
							.buyAmount(counterPartyOperation.getBankBuyAmount())
							.sellAmount(counterPartyOperation.getBankSellAmount())
							.direction(counterPartyOperation.getDirectionId().getId())
							.buyCurrencyId(counterPartyOperation.getTransactionId().getBaseCurrency().getId())
							.sellCurrencyId(counterPartyOperation.getTransactionId().getQuoteCurrency().getId())
							.insertionTime(new Timestamp(System.currentTimeMillis()))
							.build();
				
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

		InstitutionalEntity broker = institutionalEntityRepository.findById(brokerDispatchRequest.getSource()).get();

		InstitutionalEntity source = institutionalEntityRepository.findById(sourceEntity).get();

		/*
		 * NO NEED TO FALSE ANY TRANSACTION
		 * 
		 * List<CounterpartyTransactionDetails> prevTransactions =
		 * counterPartyTransactionRepository
		 * .findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
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

			List<InstitutionalEntity> listEligibleCounterparties = dal
					.findEligibleCounterparties(brokerDispatchRequest.getSource());

			listEligibleCounterparties.forEach(counterparty -> {

				if (sourceEntity != counterparty.getId()) { // Condition that order should not be circulated to the
															// entity itself

					log.info("Sending order -> " + counterparty.getLongName());

					// Setting Destination as this counterparty
					brokerDispatchRequest.setDestination(counterparty.getId());

					CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
							ApplicationConstants.Event.OUT_REQUEST.getEventId(), brokerDispatchRequest);
					// Saving w.r.t broker perspective
					counterPartyTransactionRepository.save(transactionForBroker);

					// Setting IN_REQUEST for counterparty
					CounterpartyTransactionDetails transactionForCounterparty = addToCounterPartyTransactionTable(
							ApplicationConstants.Event.IN_REQUEST.getEventId(), brokerDispatchRequest);

					counterPartyTransactionRepository.save(transactionForCounterparty);

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

		CounterpartyTransactionDetails newTransactionDetails = CounterpartyTransactionDetails.builder()

				.directionId(directionRepository.getById(counterpartyTransactionDetails.getDirection()))

				.transactionId(transactionRepository.findById(counterpartyTransactionDetails.getTransactionId()).get())

				.buyCcy(transactionRepository.findById(counterpartyTransactionDetails.getTransactionId()).get().getBaseCurrency())
				
				.sellCcy(transactionRepository.findById(counterpartyTransactionDetails.getTransactionId()).get().getQuoteCurrency())
				
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

				.requestInitiatedBy(
						institutionalEntityRepository.getById(counterpartyTransactionDetails.getRequestInitiatedBy()))

				.legInitiatedBy(institutionalEntityRepository.getById(counterpartyTransactionDetails.getSource()))

				.createdBy(userRepository.getById(counterpartyTransactionDetails.getCreatedBy()))

				.build();

		if (counterpartyTransactionDetails.getResponseInitiatedBy() == -1) {
			newTransactionDetails.setResponseInitiatedBy(null);
		} else {
			newTransactionDetails.setResponseInitiatedBy(
					institutionalEntityRepository.getById(counterpartyTransactionDetails.getResponseInitiatedBy()));
		}

		return newTransactionDetails;

	}

	@Override
	public void insertOutResponse(TransactionRequest request) {

		logTimeUpdate("insertOutResponse", START);

		try {

			InstitutionalEntity source = institutionalEntityRepository.getById(request.getSource());

			if (source.getEntityType().getId() == 2L) {

				log.info("Transaction triggered by Broker. Hence deactivating banks incoming request");

				List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevTransactions) {
					if (transactions != null && (transactions.getEventType().getId() == 2L
							|| transactions.getEventType().getId() == 4L || transactions.getEventType().getId() == 6L
							|| transactions.getEventType().getId() == 8L)) {

						transactions.setRequestActive(false);
						counterPartyTransactionRepository.save(transactions);
					} else {
						log.info("Not out coming request from bank. Seems to be on brokers side.");
					}

				}

				// Also setting previous incoming request of bank as false
				List<CounterpartyTransactionDetails> prevBankRequestTransactions = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevBankRequestTransactions) {
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
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

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

			CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.OUT_RESPONSE.getEventId(), request);
			counterPartyTransactionRepository.save(transactionForBank);

			CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.IN_RESPONSE.getEventId(), request);
			
			//initiate Matching algo
//			NewOrder newOrder = NewOrder.builder().askingRate(transactionForBroker.getClientOutright())
//								.isPartial(transactionForBroker.getTransactionId().getIsPartial())
//								.orderId(transactionForBroker.getId())
//								.buyAmount(transactionForBroker.getBankBuyAmount())
//								.sellAmount(transactionForBroker.getBankSellAmount())
//								.direction(modelMapper.map(transactionForBroker.getDirectionId(), DirectionDto.class))
//								.buyCurrency(modelMapper.map(transactionForBroker.getTransactionId().getBaseCurrency(), CurrencyDto.class))
//								.sellCurrency(modelMapper.map(transactionForBroker.getTransactionId().getQuoteCurrency(), CurrencyDto.class))
//								.insertionTime(new Timestamp(System.currentTimeMillis()))
//								.build();
//			
//			matching.initiateMatchingAlgorithm(newOrder);
			
			if(transactionForBroker.getDestinationId().getEntityType().getId() == 2L) {
				
				//Since we are matching only broker orders
				
				MatchingTable matchingTable = MatchingTable.builder()
						.askingRate(transactionForBroker.getClientOutright())
						.destinationId(transactionForBroker.getDestinationId().getId())
						.sourceId(transactionForBroker.getSourceId().getId())
						.isPartial(transactionForBroker.getTransactionId().getIsPartial())
						.orderId(transactionForBroker.getId())
						.buyAmount(transactionForBroker.getBankBuyAmount())
						.sellAmount(transactionForBroker.getBankSellAmount())
						.direction(transactionForBroker.getDirectionId().getId())
						.buyCurrencyId(transactionForBroker.getTransactionId().getBaseCurrency().getId())
						.sellCurrencyId(transactionForBroker.getTransactionId().getQuoteCurrency().getId())
						.insertionTime(new Timestamp(System.currentTimeMillis()))
						.build();
				
				orderMatch.initiateMatchingAlgorithm(matchingTable);

			
			}

			
			
			
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
			logException("insertOutResponse", e.getLocalizedMessage());
		}

		logTimeUpdate("insertOutResponse", END);

	}

	@Override
	public void insertOutNegotiation(TransactionRequest request) {

		logTimeUpdate("insertOutNegotiation", START);

		try {

			/*
			 * NO NEED TO SET FALSE
			 * 
			 * List<CounterpartyTransactionDetails> prevTransactions =
			 * counterPartyTransactionRepository
			 * .findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
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
			
			List<CounterpartyTransactionDetails> prevBankRequestTransactions = counterPartyTransactionRepository
					.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

							institutionalEntityRepository.getById(request.getDestination()),
							institutionalEntityRepository.getById(request.getSource()), true,
							transactionRepository.findById(request.getTransactionId()).get());

			for (CounterpartyTransactionDetails transactions : prevBankRequestTransactions) {
				if (transactions != null) {

					transactions.setRequestActive(false);
					counterPartyTransactionRepository.save(transactions);
				} else {
					log.info("Not out coming request from bank. Seems to be on brokers side.");
				}

			}

			if (source.getEntityType().getId() == 2L) {

				log.info("Transaction triggered by Broker. Hence deactivating banks incoming request");

//				List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
//						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
//
//								institutionalEntityRepository.getById(request.getDestination()),
//								source, true,
//								transactionRepository.findById(request.getTransactionId()).get());
//
//				for (CounterpartyTransactionDetails transactions : prevTransactions) {
//					if (transactions != null && (transactions.getEventType().getId() == 2L
//							|| transactions.getEventType().getId() == 4L || transactions.getEventType().getId() == 6L
//							|| transactions.getEventType().getId() == 8L)) {
//
//						transactions.setRequestActive(false);
//						counterPartyTransactionRepository.save(transactions);
//					} else {
//						log.info("Not out coming request from bank. Seems to be on brokers side.");
//					}
//
//				}
//
//				log.info("setting previous incoming request of bank as false");

				CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.OUT_NEGOTIATION.getEventId(), request);
				counterPartyTransactionRepository.save(transactionForBroker);

				CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.IN_NEGOTIATION.getEventId(), request);
				counterPartyTransactionRepository.save(transactionForBank);
				
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

				
		

			} else {
				log.info("Transaction triggered by Bank.");

				log.info("Creating new Transaction and forwarding to single broker");
				
				TransactionDetail parentTransaction = transactionRepository.findById(request.getTransactionId()).get();
				

				// All dates
				LocalDateTime localDateTime = LocalDateTime.now();
				Date requestTimeStamp = Date.valueOf(localDateTime.toLocalDate());

				// User

				User userId = userRepository.getById(Long.valueOf(request.getCreatedBy()));

				/*
				 * Prepare a request template
				 */
				TransactionDetail negotiaionRequest = TransactionDetail.builder()	
						.product(parentTransaction.getProduct())
						.buyAmount(parentTransaction.getBuyAmount())
						.sellAmount(parentTransaction.getSellAmount())
						.sourceOutright(parentTransaction.getSourceOutright())						
						.destinationOutright(request.getDestinationOutright())
						.tenorUnit(parentTransaction.getTenorUnit())
						.source(source)
						.baseCurrency(parentTransaction.getBaseCurrency())						
						.quoteCurrency(parentTransaction.getQuoteCurrency())
						.customer(null)
						.transactionId(UniqueIdentifierGenerator.unique()) // Saved by Db halted
						.createdAt(Instant.now()).createdBy(userId)
						.isPartial(parentTransaction.getIsPartial())
						.isConfirmed(false).build();
				
				if(parentTransaction.getBuySellDirection().getId() == 1L ) {
					log.info("Parent Transaction was a BUY");
					negotiaionRequest.setSpreadOutright(request.getSellSideSpread());
					negotiaionRequest.setBuySellDirection(directionRepository.getById(2L));
				}else {
					log.info("Parent Transaction was a SELL");
					negotiaionRequest.setSpreadOutright(request.getBuySideSpread());
					negotiaionRequest.setBuySellDirection(directionRepository.getById(1L));
				}

				TransactionDetail savedRequest = quoteRequestRepository.save(negotiaionRequest);
				
				CounterpartyTransactionDetails negotiatedTransactionForBank = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.OUT_NEGOTIATION.getEventId(), request);
				counterPartyTransactionRepository.save(negotiatedTransactionForBank);

				CounterpartyTransactionDetails negotiatedTransactionForBroker = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.IN_NEGOTIATION.getEventId(), request);
				counterPartyTransactionRepository.save(negotiatedTransactionForBroker);

				
				request.setTransactionId(savedRequest.getId()); //Setting transaction ID for new request.
				
				
				CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.OUT_REQUEST.getEventId(), request);
				counterPartyTransactionRepository.save(transactionForBank);

				CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
						ApplicationConstants.Event.IN_REQUEST.getEventId(), request);
				counterPartyTransactionRepository.save(transactionForBroker);
				
				if(transactionForBroker.getDestinationId().getEntityType().getId() == 2L) {
					
					//Since we are matching only broker orders
					
					MatchingTable matchingTable = MatchingTable.builder()
							.askingRate(transactionForBroker.getClientOutright())
							.destinationId(transactionForBroker.getDestinationId().getId())
							.sourceId(transactionForBroker.getSourceId().getId())
							.isPartial(transactionForBroker.getTransactionId().getIsPartial())
							.orderId(transactionForBroker.getId())
							.buyAmount(transactionForBroker.getBankBuyAmount())
							.sellAmount(transactionForBroker.getBankSellAmount())
							.direction(transactionForBroker.getDirectionId().getId())
							.buyCurrencyId(transactionForBroker.getTransactionId().getBaseCurrency().getId())
							.sellCurrencyId(transactionForBroker.getTransactionId().getQuoteCurrency().getId())
							.insertionTime(new Timestamp(System.currentTimeMillis()))
							.build();
					
					orderMatch.initiateMatchingAlgorithm(matchingTable);

				
				}
				
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
				
				
			}

			

			//initiate Matching algo
//			NewOrder newOrder = NewOrder.builder().askingRate(transactionForBroker.getClientOutright())
//								.orderId(transactionForBroker.getId())
//								.isPartial(transactionForBroker.getTransactionId().getIsPartial())
//								.buyAmount(transactionForBroker.getBankBuyAmount())
//								.sellAmount(transactionForBroker.getBankSellAmount())
//								.direction(modelMapper.map(transactionForBroker.getDirectionId(), DirectionDto.class))
//								.buyCurrency(modelMapper.map(transactionForBroker.getTransactionId().getBaseCurrency(), CurrencyDto.class))
//								.sellCurrency(modelMapper.map(transactionForBroker.getTransactionId().getQuoteCurrency(), CurrencyDto.class))
//								.insertionTime(new Timestamp(System.currentTimeMillis()))
//								.build();
//			
//			matching.initiateMatchingAlgorithm(newOrder);


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
			 * .findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
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

			if (source.getEntityType().getId() == 2L) {

				log.info("Transaction triggered by Broker. Hence deactivating banks incoming request");

				List<CounterpartyTransactionDetails> prevTransactions = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevTransactions) {
					if (transactions != null && (transactions.getEventType().getId() == 2L
							|| transactions.getEventType().getId() == 4L || transactions.getEventType().getId() == 6L
							|| transactions.getEventType().getId() == 8L)) {

						transactions.setRequestActive(false);
						counterPartyTransactionRepository.save(transactions);
					} else {
						log.info("Not out coming request from bank. Seems to be on brokers side.");
					}

				}

				List<CounterpartyTransactionDetails> prevBankRequestTransactions = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

								institutionalEntityRepository.getById(request.getDestination()),
								institutionalEntityRepository.getById(request.getSource()), true,
								transactionRepository.findById(request.getTransactionId()).get());

				for (CounterpartyTransactionDetails transactions : prevBankRequestTransactions) {
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
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(

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

			if (buySideCounterparty.getId() == buyTransaction.getSource().getId()) { // transaction

				buyTransaction.setIsConfirmed(true);
				transactionRepository.save(buyTransaction);


			}

			if (sellSideCounterparty.getId() == sellTransaction.getSource().getId()) { // transaction

				sellTransaction.setIsConfirmed(true);
				transactionRepository.save(sellTransaction);


			}

			if (buySideCounterparty.getId() != buyTransaction.getSource().getId()
					&& sellSideCounterparty.getId() != sellTransaction.getSource().getId()
					&& buySideCounterparty.getEntityType().getId() == 1L
					&& sellSideCounterparty.getEntityType().getId() == 1L) {
				// Add new Transaction to Db

				TransactionDetail newBuyTransaction = TransactionDetail.builder()
						.baseCurrency(buySideCounterPartyTransaction.getTransactionId().getBaseCurrency())
						.quoteCurrency(buySideCounterPartyTransaction.getTransactionId().getQuoteCurrency())
						.buyAmount(buySideCounterPartyTransaction.getBankBuyAmount())
						.sellAmount(buySideCounterPartyTransaction.getBankSellAmount())
						.buySellDirection(buySideCounterPartyTransaction.getDirectionId())
						.createdBy(buySideCounterPartyTransaction.getCreatedBy())
						.destinationOutright(buySideCounterPartyTransaction.getClientOutright()).isConfirmed(true)
						.isPartial(buySideCounterPartyTransaction.getTransactionId().getIsPartial()).build();

				TransactionDetail newSellTransaction = TransactionDetail.builder()
						.baseCurrency(sellSideCounterPartyTransaction.getTransactionId().getBaseCurrency())
						.quoteCurrency(sellSideCounterPartyTransaction.getTransactionId().getQuoteCurrency())
						.buyAmount(sellSideCounterPartyTransaction.getBankBuyAmount())
						.sellAmount(sellSideCounterPartyTransaction.getBankSellAmount())
						.buySellDirection(sellSideCounterPartyTransaction.getDirectionId())
						.createdBy(sellSideCounterPartyTransaction.getCreatedBy())
						.destinationOutright(sellSideCounterPartyTransaction.getClientOutright()).isConfirmed(true)
						.isPartial(sellSideCounterPartyTransaction.getTransactionId().getIsPartial()).build();

				transactionRepository.save(newBuyTransaction);
				transactionRepository.save(newSellTransaction);

			

			}

			ConfirmedOrderDetails buyConfirm = ConfirmedOrderDetails.builder().brokerId(broker)
					.buySideCounterpartyTransactionId(buySideCounterPartyTransaction)
					.sellSideCounterpartyTransactionId(sellSideCounterPartyTransaction)
					.sellSideId(sellSideCounterparty).buySideId(buySideCounterparty).transactionId(buyTransaction)
					.createdBy(userRepository.findById(confirmRequest.getCreatedBy()).get())
					.build();

			confirmationRepostory.save(buyConfirm);

			ConfirmedOrderDetails sellConfirm = ConfirmedOrderDetails.builder().brokerId(broker)
					.buySideCounterpartyTransactionId(buySideCounterPartyTransaction)
					.sellSideCounterpartyTransactionId(sellSideCounterPartyTransaction)
					.sellSideId(sellSideCounterparty).buySideId(buySideCounterparty).transactionId(sellTransaction)
					.createdBy(userRepository.findById(confirmRequest.getCreatedBy()).get())
					.build();
			confirmationRepostory.save(sellConfirm);
			
			// Deactivating counterPartyBroker-Broker transaction

			List<CounterpartyTransactionDetails> buySideTransactionActive = counterPartyTransactionRepository
					.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
							buySideCounterPartyTransaction.getSourceId(), broker, true, buyTransaction);

			for (CounterpartyTransactionDetails buySideTransaction : buySideTransactionActive) {
				if (buySideTransaction != null) {
					buySideTransaction.setRequestActive(false);
					counterPartyTransactionRepository.save(buySideTransaction);
				}

			}

			List<CounterpartyTransactionDetails> sellSideTransactionActive = counterPartyTransactionRepository
					.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
							sellSideCounterPartyTransaction.getSourceId(), broker, true, sellTransaction);

			for (CounterpartyTransactionDetails sellSideTransaction : sellSideTransactionActive) {
				if (sellSideTransaction != null) {
					sellSideTransaction.setRequestActive(false);
					counterPartyTransactionRepository.save(sellSideTransaction);
				}

			}

			if (buySideCounterparty.getEntityType().getId() == 2L) {

				// Set everytime request initiated if Bank
				// Set everytime same requestInitiatedBy while broker sends to broker

				List<CounterpartyTransactionDetails> buytransaction = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
								buySideCounterPartyTransaction.getLegInitiatedBy(), buySideCounterparty, true,
								buyTransaction);

				List<CounterpartyTransactionDetails> selltransaction = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(broker, buySideCounterparty,
								true, buyTransaction);

				for (CounterpartyTransactionDetails transaction : buytransaction) {
					if (transaction != null) {
						transaction.setRequestActive(false);
						counterPartyTransactionRepository.save(transaction);
					}
				}

				for (CounterpartyTransactionDetails transaction : selltransaction) {
					if (transaction != null) {
						transaction.setRequestActive(false);
						counterPartyTransactionRepository.save(transaction);
					}
				}

				ConfirmRequest newRequest = ConfirmRequest.builder().brokerId(buySideCounterparty.getId())
						.buySideCounterParty(buySideCounterPartyTransaction.getLegInitiatedBy().getId())
						.sellSideCounterPart(broker.getId())
						.buySideCounterPartyTransactionId(buytransaction.get(0).getId())
						.sellSideCounterPartyTransactionId(selltransaction.get(0).getId()).build();

				confirmRequest(newRequest);

			}
			if (sellSideCounterparty.getEntityType().getId() == 2L) {

				// Set everytime request initiated if Bank
				// Set everytime same requestInitiatedBy while broker sends to broker

				List<CounterpartyTransactionDetails> buytransaction = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(
								sellSideCounterPartyTransaction.getLegInitiatedBy(), sellSideCounterparty, true,
								sellTransaction);

				List<CounterpartyTransactionDetails> selltransaction = counterPartyTransactionRepository
						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(broker, sellSideCounterparty,
								true, sellTransaction);

				for (CounterpartyTransactionDetails transaction : buytransaction) {
					if (transaction != null) {
						transaction.setRequestActive(false);
						counterPartyTransactionRepository.save(transaction);
					}
				}

				for (CounterpartyTransactionDetails transaction : selltransaction) {
					if (transaction != null) {
						transaction.setRequestActive(false);
						counterPartyTransactionRepository.save(transaction);
					}
				}

				ConfirmRequest newRequest = ConfirmRequest.builder().brokerId(buySideCounterparty.getId())
						.buySideCounterParty(buySideCounterPartyTransaction.getLegInitiatedBy().getId())
						.sellSideCounterPart(broker.getId())
						.buySideCounterPartyTransactionId(buytransaction.get(0).getId())
						.sellSideCounterPartyTransactionId(selltransaction.get(0).getId()).build();

				confirmRequest(newRequest);

			}

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
//					.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(sellSideCPTradeDetails.getSourceId(), broker, true,
//							sellTransaction);
//
//			for (CounterpartyTransactionDetails sellSideTransaction : sellSideTransactionActive) {
//				sellSideTransaction.setRequestActive(false);
//				counterPartyTransactionRepository.save(sellSideTransaction);
//			}
//
//			List<CounterpartyTransactionDetails> buySideTransactionActive = counterPartyTransactionRepository
//					.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(buySideCPTradeDetails.getSourceId(), broker, true,
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
//						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(requestInitiatedBy,
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
//						.findBysourceIdAndDestinationIdAndIsRequestActiveAndTransactionId(requestInitiatedBy,
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
	

    // Helper method to copy properties from one object to another excluding specified properties
    private static void copyPropertiesExclude(Object source, Object destination, String... excludeProperties) {
        Class<?> sourceClass = source.getClass();
        Class<?> destinationClass = destination.getClass();

        // Get all fields from the source class
        Field[] sourceFields = sourceClass.getDeclaredFields();

        for (Field field : sourceFields) {
            // Skip fields that are in the exclude list
            if (contains(excludeProperties, field.getName())) {
                continue;
            }

            // Copy the value from the source field to the destination field
            try {
                field.setAccessible(true);
                Field destinationField = destinationClass.getDeclaredField(field.getName());
                destinationField.setAccessible(true);
                destinationField.set(destination, field.get(source));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to check if an array contains a specific string
    private static boolean contains(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue)) {
                return true;
            }
        }
        return false;
    }
}


