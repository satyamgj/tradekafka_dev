package com.tradekafka.ondc.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.finmamba.entities.ondc.ConfirmedTransactions;
import com.finmamba.entities.ondc.MatchingTransactions;
import com.finmamba.entities.ondc.RequestedOrders;
import com.finmamba.entities.staticdata.Currency;
import com.finmamba.entities.staticdata.Direction;
import com.finmamba.entities.transaction.User;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.bus.HttpResponseModel;
import com.finmamba.models.ondc.ConfirmRequest;
import com.finmamba.models.ondc.MatchNotification;
import com.finmamba.models.ondc.OrderForMatchEngine;
import com.finmamba.models.ondc.QuoteRequest;
import com.finmamba.models.ondc.SellerCatalogue;
import com.tradekafka.ondc.repository.ConfirmRequestRepository;
import com.tradekafka.ondc.repository.MatchingTransactionRepository;
import com.tradekafka.ondc.repository.QuoteRequestRepository;
import com.tradekafka.ondc.repository.RequestOrderRepository;
import com.tradekafka.ondc.repository.SellerCatalogueRepository;
import com.tradekafka.ondc.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OndcServices implements IOndcServices {


	@Autowired
	UserRepository userRepository;

	@Autowired
	QuoteRequestRepository quoteRequestRepository;

	@Autowired
	RequestOrderRepository requestOrderRepository;

	@Autowired
	IMatchingService matchingService;

	@Autowired
	MatchingTransactionRepository matchingTransactionRepository;

	@Autowired
	ConfirmRequestRepository confirmRequestRepository;

	@Autowired
	SellerCatalogueRepository catalogueRepository;
	
	@Autowired
	KafkaProducerService kafkaProducerService;
	
	private static final String topic = "notification";

	private final CurrencyStaticMap currencyStaticMap;

	private final DirectionStaticData directionStaticData;

	Map<Long, Currency> ccy = new HashMap<>();
	Map<Long, Direction> direction = new HashMap<>();

	OndcServices(CurrencyStaticMap currencyStaticMap, DirectionStaticData directionStaticData) {
		this.currencyStaticMap = currencyStaticMap;
		this.directionStaticData = directionStaticData;

		ccy = this.currencyStaticMap.getCurrencyMap();
		direction = directionStaticData.getDirectionMap();
	}

	@Override
	public HttpResponseModel quoteRequest(QuoteRequest quoteRequest) {
		try {
			startTime("quoteRequest");

			log.info("Will hit RBI API for validity check");

//			Currency baseCurrency = currencyRepository.findById(quoteRequest.getBaseCurrencyId()).get();
//			Currency quoteCurrency = currencyRepository.findById(quoteRequest.getQuoteCurrencyId()).get();

			Currency baseCurrency = ccy.get(quoteRequest.getBaseCurrencyId());
			Currency quoteCurrency = ccy.get(quoteRequest.getQuoteCurrencyId());

			User dealPlacer = userRepository.getById(quoteRequest.getUserId());
			
			com.finmamba.entities.ondc.QuoteRequest newRequest = com.finmamba.entities.ondc.QuoteRequest.builder()
					.amount(quoteRequest.getAmount()).baseCurrency(baseCurrency).quoteCurrency(quoteCurrency)
					.buyUserId(dealPlacer)
					.payableCcy(null)
					.directionId(direction.get(quoteRequest.getDirectionId())).isActive(true).isConfirmed(false)
					.isMatched(false).responseYN(quoteRequest.getResponseYN()).askingRate(quoteRequest.getAskingRate())
					.build();

			if(quoteRequest.getResponseYN().equalsIgnoreCase( "Y") ) {
				com.finmamba.entities.ondc.QuoteRequest responseTo = quoteRequestRepository.findById(quoteRequest.getResponseTo()).get();
				newRequest.setResponseTo(responseTo);
				RequestedOrders request = requestOrderRepository.findByUserIdAndQuoteRequestId(dealPlacer, responseTo);
				if(request!=null) {
					request.setIsActive(false);
					request.setResponseRate(quoteRequest.getAskingRate());
					requestOrderRepository.save(request);
				
				}
			}
			
			quoteRequestRepository.save(newRequest);

			if (quoteRequest.getResponseYN().equalsIgnoreCase("N")) {

				if(quoteRequest.getDirectionId() == 1L) {

					log.info("Broadcasting to sellers acc to sell catalogue");

					broadcastToSellers(baseCurrency, quoteCurrency, newRequest);

				}

				log.info("Also initiating matching for this new order");

				OrderForMatchEngine newOrderForMatchEngine = OrderForMatchEngine.builder()
						.amount(quoteRequest.getAmount()).askingRate(quoteRequest.getAskingRate())
						.baseCurrency(baseCurrency.getIsoCode()).quoteCurrency(quoteCurrency.getIsoCode())
						.directionId(quoteRequest.getDirectionId()).quoteRequestId(newRequest.getId())
						.initiatorId(quoteRequest.getUserId()).build();

				matchingService.matchingEngine(newOrderForMatchEngine);

			} else {
				log.info("This seems to be a response from seller " + quoteRequest.getUserId() + " for quote "
						+ quoteRequest.getResponseTo());
				log.info("Will push this order to matching table");

				com.finmamba.entities.ondc.QuoteRequest request = quoteRequestRepository
						.findById(quoteRequest.getResponseTo()).get();
				MatchingTransactions newMatch = MatchingTransactions.builder().buyTxnId(request).sellTxnId(newRequest)
						.build();
				matchingTransactionRepository.save(newMatch);
			}

			endTime("quoteRequest");

			return HttpResponseModel.builder()
					// .data(newRequest)
					.message("Quote Request sent successfully").build();

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Exception occured inside quoteRequest " + e.getMessage());
			return HttpResponseModel.builder().data(null).message("Something went wrong while sending your quote")
					.status(304).build();
		} finally {
			endTime("quoteRequest");
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

	@Transactional
	@Override
	public HttpResponseModel confirmRequest(ConfirmRequest confirmRequest) {
		try {

			startTime("confirmRequest");

			com.finmamba.entities.ondc.QuoteRequest buyTrxn = quoteRequestRepository
					.findById(confirmRequest.getBuyTrxnId()).get();
			com.finmamba.entities.ondc.QuoteRequest sellTrxn = quoteRequestRepository
					.findById(confirmRequest.getSellTrxnId()).get();

			if (!buyTrxn.getIsConfirmed() && !sellTrxn.getIsConfirmed()) {
				ConfirmedTransactions confirmTransaction = ConfirmedTransactions.builder().buyTxnId(buyTrxn)
						.sellTxnId(sellTrxn).rate(sellTrxn.getAskingRate()).build();

				confirmRequestRepository.save(confirmTransaction);
				buyTrxn.setIsConfirmed(true);
				buyTrxn.setIsActive(false);
				//buyTrxn.setPayableAmount(buyTrxn.getAmount().multiply(sellTrxn.getAskingRate()));
				sellTrxn.setIsConfirmed(true);
				sellTrxn.setIsActive(false);
				//sellTrxn.setPayableAmount(sellTrxn.getAmount().multiply(sellTrxn.getAskingRate()));
				
				if(buyTrxn.getDirectionId().getId()==2) {
					buyTrxn.setPayableAmount(buyTrxn.getAmount());
					buyTrxn.setPayableCcy(buyTrxn.getBaseCurrency());
					buyTrxn.setReceivableAmount(buyTrxn.getAmount().multiply(sellTrxn.getAskingRate()));
					buyTrxn.setReceivableCcy(buyTrxn.getQuoteCurrency());
				}else {
					buyTrxn.setPayableAmount(buyTrxn.getAmount().multiply(sellTrxn.getAskingRate()));
					buyTrxn.setPayableCcy(buyTrxn.getQuoteCurrency());
					buyTrxn.setReceivableAmount(buyTrxn.getAmount());
					buyTrxn.setReceivableCcy(buyTrxn.getBaseCurrency());
				}
				
				if(sellTrxn.getDirectionId().getId()==2) {
					sellTrxn.setPayableAmount(sellTrxn.getAmount());
					sellTrxn.setPayableCcy(sellTrxn.getBaseCurrency());
					sellTrxn.setReceivableAmount(buyTrxn.getAmount().multiply(sellTrxn.getAskingRate()));
					sellTrxn.setReceivableCcy(buyTrxn.getQuoteCurrency());
				}else {
					sellTrxn.setPayableAmount(sellTrxn.getAmount().multiply(sellTrxn.getAskingRate()));
					sellTrxn.setPayableCcy(sellTrxn.getQuoteCurrency());
					sellTrxn.setReceivableAmount(sellTrxn.getAmount());
					sellTrxn.setReceivableCcy(sellTrxn.getBaseCurrency());
				}
				
				buyTrxn.setFinalPrice(sellTrxn.getAskingRate());
				sellTrxn.setFinalPrice(sellTrxn.getAskingRate());
				
				quoteRequestRepository.saveAndFlush(buyTrxn);
				quoteRequestRepository.saveAndFlush(sellTrxn);

				OrderForMatchEngine buyOrder = OrderForMatchEngine.builder()
						.baseCurrency(buyTrxn.getBaseCurrency().getIsoCode())
						.quoteCurrency(buyTrxn.getQuoteCurrency().getIsoCode())
						.directionId(buyTrxn.getDirectionId().getId()).build();
				matchingService.removeConfirmedOrder(buyOrder);

				OrderForMatchEngine sellOrder = OrderForMatchEngine.builder()
						.baseCurrency(sellTrxn.getBaseCurrency().getIsoCode())
						.quoteCurrency(sellTrxn.getQuoteCurrency().getIsoCode())
						.directionId(sellTrxn.getDirectionId().getId()).build();
				matchingService.removeConfirmedOrder(sellOrder);

			} else {
				log.info("Price not available. Please select different quote price.");
				return HttpResponseModel.builder().data(null)
						.message("Price not available. Please select different quote price ").status(304).build();

			}

			endTime("confirmRequest");

			return HttpResponseModel.builder().data(null).message("Quote confirmed successfully!").build();

		} catch (Exception e) {
			log.info("Exception occured inside confirmRequest " + e.getMessage());

			endTime("confirmRequest");

			return HttpResponseModel.builder().data(null).message("Something went wrong while sending your quote")
					.status(304).build();
		}
	}

	@Override
	public HttpResponseModel fetchMyRequests(Long userId) {
		try {

			User user = userRepository.findById(userId).get();

			return HttpResponseModel.builder().data(quoteRequestRepository.findByBuyUserIdAndIsConfirmed(user,false))
					.message("Fetched quote requests successfully!").build();

		} catch (Exception e) {
			log.info("Exception occured inside fetchMyRequest " + e.getMessage());

			return HttpResponseModel.builder().data(null)
					.message("Something went wrong while fetching your active quotes").status(304).build();
		}
	}

	@Override
	public HttpResponseModel fetchMatchedRequests(Long quoteId) {
		try {

			com.finmamba.entities.ondc.QuoteRequest quote = quoteRequestRepository.findById(quoteId).get();

			if(quote.getDirectionId().getId() == 1) {
				return HttpResponseModel.builder().data(matchingTransactionRepository.findByBuyTxnId(quote))
						.message("Fetched quote requests successfully!").build();
			}else {
				return HttpResponseModel.builder().data(matchingTransactionRepository.findBySellTxnId(quote))
						.message("Fetched quote requests successfully!").build();
			}
			

		} catch (Exception e) {
			log.info("Exception occured inside fetchMatchedRequest " + e.getMessage());
			endTime("fetchMatchedRequests");

			return HttpResponseModel.builder().data(null).message("Something went wrong while fetching matched quotes")
					.status(304).build();
		}
	}

	@Override
	public HttpResponseModel fetchConfirmedQuotes(Long userId) {
		try {

			User user = userRepository.findById(userId).get();

			return HttpResponseModel.builder().data(quoteRequestRepository.findByBuyUserIdAndIsConfirmed(user, true))
					.message("Fetched quote requests successfully!").build();

		} catch (Exception e) {
			log.info("Exception occured inside fetchConfirmedQuotes " + e.getMessage());
			return HttpResponseModel.builder().data(null).message("Something went wrong while sending your quote")
					.status(304).build();
		}
	}

	@Override
	public HttpResponseModel addSellerCatalogue(SellerCatalogue catalogue) {
		try {

			startTime("addSellerCatalogue");

			User user = userRepository.findById(catalogue.getUserId()).get();
			Currency buyCcy = ccy.get(catalogue.getBaseCcyId());
			Currency sellCcy = ccy.get(catalogue.getQuoteCcyId());

			com.finmamba.entities.ondc.SellerCatalogue newEntry = com.finmamba.entities.ondc.SellerCatalogue.builder()
					.baseCurrency(buyCcy).quoteCurrency(sellCcy).userId(user).build();

			endTime("addSellerCatalogue");

			catalogueRepository.save(newEntry);
			return HttpResponseModel.builder()
					// .data(quoteRequestRepository.findByBuyUserIdAndIsConfirmed(user,true))
					.message("added to catalogue successfully!").build();

		} catch (Exception e) {
			log.info("Exception occured inside addSellerCatalogue " + e.getMessage());

			endTime("addSellerCatalogue");

			return HttpResponseModel.builder().data(null)
					.message("Something went wrong while adding to your seller catalogue").status(304).build();
		}
	}

	@Override
	public HttpResponseModel fetchSellerCatalogue(Long userId) {
		try {
			User user = userRepository.findById(userId).get();
			return HttpResponseModel.builder().data(catalogueRepository.findByUserId(user))
					.message("Fetched sell catalogues successfully!").build();

		} catch (Exception e) {
			log.info("Exception occured inside fetchConfirmedQuotes " + e.getMessage());
			return HttpResponseModel.builder().data(null)
					.message("Something went wrong while fetching your seller catalogue").status(304).build();
		}
	}

	@Async
	@Override
	public void broadcastToSellers(Currency buyCcy, Currency sellCcy,
			com.finmamba.entities.ondc.QuoteRequest quoteRequest) {
		try {

			startTime("broadcastToSellers");

			String ccyPair = buyCcy.getIsoCode() + "/" + sellCcy.getIsoCode();

			List<com.finmamba.entities.ondc.SellerCatalogue> sellers = catalogueRepository
					.findByBaseCurrencyAndQuoteCurrency(buyCcy, sellCcy);
			if (sellers.size() > 0) {

				log.info("Found " + sellers.size() + " sellers for " + ccyPair);

				for (com.finmamba.entities.ondc.SellerCatalogue seller : sellers) {
					RequestedOrders newRequest = RequestedOrders.builder().quoteRequestId(quoteRequest)
							.isActive(true)
							.userId(seller.getUserId()).build();
					requestOrderRepository.save(newRequest);
					
					MatchNotification notificationToUser= MatchNotification.builder()
							.userId(seller.getUserId().getId())
							.message("Received a new order for ccy " + ccyPair + " @ " + quoteRequest.getAskingRate() 
								+ " for amount " + quoteRequest.getAmount()
									)
							.build();
					
					pushNotification(notificationToUser);
				}
			} else {
				log.info("Found no sellers for " + ccyPair);
			}

			endTime("broadcastToSellers");

		} catch (Exception e) {
			log.info("Exception occured inside broadcastToSellers " + e.getMessage());

			endTime("broadcastToSellers");

		}
	}

	@Override
	public HttpResponseModel fetchRequestedQuotes(Long userId) {
		try {

			User user = userRepository.findById(userId).get();
			return HttpResponseModel.builder().data(requestOrderRepository.findByUserId(user))
					.message("Fetched quote requests successfully!").build();

		} catch (Exception e) {
			log.info("Exception occured inside fetchRequestedQuotes " + e.getMessage());
			return HttpResponseModel.builder().data(null)
					.message("Something went wrong while fetching your seller catalogue").status(304).build();
		}
	}

	@Override
	public HttpResponseModel alreadyRespondedToQuote(Long userId, Long quoteId) {
		try {
			User user = userRepository.findById(userId).get();
			com.finmamba.entities.ondc.QuoteRequest quote = quoteRequestRepository.findById(quoteId).get();
			
			Boolean alreadyResponded = quoteRequestRepository.existsByResponseToAndBuyUserId(quote, user);
			
			
			return HttpResponseModel.builder().data(alreadyResponded)
					.message("Fetched response status successfully!").build();
			
		} catch (Exception e) {
			log.info("Exception occured inside alreadyRespondedToQuote " + e.getMessage());
			return HttpResponseModel.builder().data(false)
					.message("Something went wrong while executing alreadyRespondedToQuote method").status(304).build();
		}
		
		
	}
	

	@Async
	public void pushNotification(MatchNotification matchNotification) {
		try {
			kafkaProducerService.send(matchNotification, topic, null, null);

		} catch (Exception e) {
			// TODO: handle exception
			log.info("exception occured inside pushNotification " + e.getMessage());

		}finally {
			log.info("Sent " + matchNotification.getMessage() + " to userId -> " + matchNotification.getUserId());
		}
		
	}
}
