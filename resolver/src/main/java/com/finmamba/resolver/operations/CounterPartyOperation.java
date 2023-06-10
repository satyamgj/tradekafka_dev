package com.finmamba.resolver.operations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.configuration.EntityConfigurationSettings;
import com.finmamba.entities.staticdata.InstitutionalEntity;
import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.CounterpartyTransactionDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.models.dto.SellSideTransactionDto;
import com.finmamba.models.transaction.TransactionRequest;
import com.finmamba.resolver.repositories.CounterPartyTransactionRepository;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.EntityConfigurationRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.QuoteRequestRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.services.EventPublisher;
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

				EntityConfigurationSettings brokerConfiguration = entityConfigurationRepository
						.findByEntityId(broker.getId());

				CounterpartyTransactionDetails counterPartyOperation = CounterpartyTransactionDetails.builder()

						.transactionId(quoteRequestDto.getTransactionId())

						.sourceId(institutionalEntityRepository.getById(quoteRequestDto.getSource().getId()))

						.destinationId(institutionalEntityRepository.getById(broker.getId()))

						.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId()))

						.bankBuyAmount(quoteRequestDto.getBuyAmount()).bankSellAmount(quoteRequestDto.getSellAmount())

						.clientOutright(quoteRequestDto.getDestinationOutright()).isRequestActive(true)

						.createdBy(userRepository.getById(quoteRequestDto.getCreatedBy().getId()))

						.isPartial(quoteRequestDto.getIsPartial())

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

			logException(functionName, functionName);

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
		
		CounterpartyTransactionDetails previousActiveTransaction = counterPartyTransactionRepository
				.findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId(
						 source
						,broker
						, true
						, brokerDispatchRequest.getTransactionId());

		// Deactivating previous Active request of this transaction for broker
		previousActiveTransaction.setRequestActive(false);
		
		counterPartyTransactionRepository.save(previousActiveTransaction);
		
		try {

			List<InstitutionalEntity> listEligibleCounterparties = dal
					.findEligibleCounterparties(brokerDispatchRequest.getSource());

			listEligibleCounterparties.forEach(counterparty -> {
				
				if(sourceEntity != counterparty.getId()) { //Condition that order should not be circulated to the entity itself
					
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

			logException("dispatchRequestToCounterparties", e.getMessage());

			logTimeUpdate("dispatchRequestToCounterparties", END);

			return false;
		}

	}

	public CounterpartyTransactionDetails addToCounterPartyTransactionTable(Long eventId,
			TransactionRequest counterpartyTransactionDetails) {

		CounterpartyTransactionDetails newTransactionDetails = CounterpartyTransactionDetails.builder()

				.transactionId(counterpartyTransactionDetails.getTransactionId())

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

				.isPartial(counterpartyTransactionDetails.getIsPartial())

				.requestInitiatedBy(
						institutionalEntityRepository.getById(counterpartyTransactionDetails.getResponseInitiatedBy()))

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
			CounterpartyTransactionDetails prevTransactionAtSource = counterPartyTransactionRepository
					.findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId(
							institutionalEntityRepository.getById(request.getSource())
							,institutionalEntityRepository.getById(request.getDestination())
							, true
							, request.getTransactionId());

			if (prevTransactionAtSource != null) {
				prevTransactionAtSource.setRequestActive(false);
			} else {
				log.info("No previous transaction found!");
			}
						
			counterPartyTransactionRepository.save(prevTransactionAtSource);
			
			//Disable transaction at destination
			
			CounterpartyTransactionDetails prevTransactionAtDestination = counterPartyTransactionRepository.findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId
					(
							institutionalEntityRepository.getById(request.getDestination()), 
							institutionalEntityRepository.getById(request.getSource()),
							true,
							request.getTransactionId());
			
			if (prevTransactionAtDestination != null) {
				prevTransactionAtDestination.setRequestActive(false);
			} else {
				log.info("No previous transaction found!");
			}
			
			counterPartyTransactionRepository.save(prevTransactionAtDestination);

			CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.OUT_RESPONSE.getEventId(), request);
			counterPartyTransactionRepository.save(transactionForBank);

			CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.IN_RESPONSE.getEventId(), request);

			counterPartyTransactionRepository.save(transactionForBroker);

			InstitutionalEntity broker = institutionalEntityRepository.getById(request.getDestination());
			
			// Publish on kafka topic
			String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "."
					+ broker.getClientRoleName() + "." + broker.getGroupName();

			log.info("Pushing on kafka topic: " + kafkaTopic);

			CounterpartyTransactionDto inResponse = modelMapper.map(transactionForBroker, CounterpartyTransactionDto.class);

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
			
			CounterpartyTransactionDetails prevTransactionAtSource = counterPartyTransactionRepository
					.findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId(
							institutionalEntityRepository.getById(request.getSource())
							,institutionalEntityRepository.getById(request.getDestination())
							, true
							, request.getTransactionId());

			if (prevTransactionAtSource != null) {
				prevTransactionAtSource.setRequestActive(false);
			} else {
				log.info("No previous transaction found!");
			}

			counterPartyTransactionRepository.save(prevTransactionAtSource);
			
			//Disable transaction at destination
			
			CounterpartyTransactionDetails prevTransactionAtDestination = counterPartyTransactionRepository.findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId
					(
							institutionalEntityRepository.getById(request.getDestination()), 
							institutionalEntityRepository.getById(request.getSource()), true, request.getTransactionId());
			
			if (prevTransactionAtDestination != null) {
				prevTransactionAtDestination.setRequestActive(false);
			} else {
				log.info("No previous transaction found!");
			}
			
			counterPartyTransactionRepository.save(prevTransactionAtDestination);

			CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.OUT_NEGOTIATION.getEventId(), request);
			counterPartyTransactionRepository.save(transactionForBank);

			CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.IN_NEGOTIATION.getEventId(), request);

			counterPartyTransactionRepository.save(transactionForBroker);

			InstitutionalEntity broker = institutionalEntityRepository.getById(request.getDestination());
			
			// Publish on kafka topic
			String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "."
					+ broker.getClientRoleName() + "." + broker.getGroupName();

			log.info("Pushing on kafka topic: " + kafkaTopic);

			CounterpartyTransactionDto inResponse = modelMapper.map(transactionForBroker, CounterpartyTransactionDto.class);

			List<CounterpartyTransactionDto> inResponseList = new ArrayList<>();
			inResponseList.add(inResponse);

			kafkaProducerService.send(inResponseList, kafkaTopic, null, null);
			
			
		} catch (Exception e) {
			logException("insertOutNegotiation", e.getLocalizedMessage());
		}
		
		logTimeUpdate("insertOutNegotiation", END);

	}

	@Override
	public void insertOutReject(TransactionRequest request) {
logTimeUpdate("insertOutNegotiation", START);
		
		try {
			
			CounterpartyTransactionDetails prevTransactionAtSource = counterPartyTransactionRepository
					.findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId(
							institutionalEntityRepository.getById(request.getSource())
							,institutionalEntityRepository.getById(request.getDestination())
							, true
							, request.getTransactionId());

			if (prevTransactionAtSource != null) {
				prevTransactionAtSource.setRequestActive(false);
			} else {
				log.info("No previous transaction found!");
			}

			counterPartyTransactionRepository.save(prevTransactionAtSource);
			
			//Disable transaction at destination
			
			CounterpartyTransactionDetails prevTransactionAtDestination = counterPartyTransactionRepository.findBysourceIdAnddestinationIdAndIsRequestActiveAndTransactionId
					(
							institutionalEntityRepository.getById(request.getDestination()), 
							institutionalEntityRepository.getById(request.getSource()), true, request.getTransactionId());
			
			if (prevTransactionAtDestination != null) {
				prevTransactionAtDestination.setRequestActive(false);
			} else {
				log.info("No previous transaction found!");
			}
			
			counterPartyTransactionRepository.save(prevTransactionAtDestination);
			CounterpartyTransactionDetails transactionForBank = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.OUT_REJECT.getEventId(), request);
			counterPartyTransactionRepository.save(transactionForBank);

			CounterpartyTransactionDetails transactionForBroker = addToCounterPartyTransactionTable(
					ApplicationConstants.Event.IN_REJECT.getEventId(), request);

			counterPartyTransactionRepository.save(transactionForBroker);

			InstitutionalEntity broker = institutionalEntityRepository.getById(request.getDestination());
			
			// Publish on kafka topic
			String kafkaTopic = broker.getRealmName() + "." + broker.getClientName() + "."
					+ broker.getClientRoleName() + "." + broker.getGroupName();

			log.info("Pushing on kafka topic: " + kafkaTopic);

			CounterpartyTransactionDto inResponse = modelMapper.map(transactionForBroker, CounterpartyTransactionDto.class);

			List<CounterpartyTransactionDto> inResponseList = new ArrayList<>();
			inResponseList.add(inResponse);

			kafkaProducerService.send(inResponseList, kafkaTopic, null, null);
			
			
		} catch (Exception e) {
			logException("insertOutNegotiation", e.getLocalizedMessage());
		}
		
		logTimeUpdate("insertOutNegotiation", END);
	}

}
