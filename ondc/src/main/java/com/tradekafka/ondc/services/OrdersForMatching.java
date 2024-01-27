package com.tradekafka.ondc.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.finmamba.entities.ondc.QuoteRequest;
import com.finmamba.models.ondc.OrderForMatchEngine;
import com.tradekafka.ondc.repository.DirectionRepository;
import com.tradekafka.ondc.repository.QuoteRequestRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrdersForMatching {
	
	@Autowired
	IMatchingService matchingService;
	
	@Autowired
	QuoteRequestRepository quoteRequestRepository;
	
	@Autowired
	DirectionRepository directionRepository;

	@PostConstruct
	public void initializeOrderQueueForMatching() {
		
		log.info("Started initializing Order Queue");
		
		log.info("Triggered @postConstruct call initializeOrderQueueForMatching ");
		  // Fetch buy and sell orders from the database asynchronously
        CompletableFuture<List<OrderForMatchEngine>> buyOrdersFuture = CompletableFuture.supplyAsync(() -> 
        	
        {
            List<OrderForMatchEngine> buyOrders = new ArrayList<>();
            List<QuoteRequest>activeBuyOrders = quoteRequestRepository
            		.findByDirectionIdAndIsConfirmed(directionRepository.findById(1L).get(), false);
            for(QuoteRequest quoteRequest : activeBuyOrders) {
            	OrderForMatchEngine newOrderForMatching = OrderForMatchEngine.builder()
						.amount(quoteRequest.getAmount()).askingRate(quoteRequest.getAskingRate())
						.baseCurrency(quoteRequest.getBaseCurrency().getIsoCode()).quoteCurrency(quoteRequest.getQuoteCurrency().getIsoCode())
						.directionId(quoteRequest.getDirectionId().getId()).quoteRequestId(quoteRequest.getId())
						.initiatorId(quoteRequest.getBuyUserId().getId()).build();
            	buyOrders.add(newOrderForMatching);
            }
            return buyOrders;
        }
        
        	
        		);
        CompletableFuture<List<OrderForMatchEngine>> sellOrdersFuture = CompletableFuture.supplyAsync(() -> 
        {
            List<OrderForMatchEngine> sellOrders = new ArrayList<>();
            List<QuoteRequest>activeSellOrders = quoteRequestRepository
            		.findByDirectionIdAndIsConfirmed(directionRepository.findById(2L).get(), false);
            for(QuoteRequest quoteRequest : activeSellOrders) {
            	OrderForMatchEngine newOrderForMatching = OrderForMatchEngine.builder()
						.amount(quoteRequest.getAmount()).askingRate(quoteRequest.getAskingRate())
						.baseCurrency(quoteRequest.getBaseCurrency().getIsoCode()).quoteCurrency(quoteRequest.getQuoteCurrency().getIsoCode())
						.directionId(quoteRequest.getDirectionId().getId()).quoteRequestId(quoteRequest.getId())
						.initiatorId(quoteRequest.getBuyUserId().getId()).build();
            	sellOrders.add(newOrderForMatching);
            }
            return sellOrders;
        }
        		);

        // Wait for both tasks to complete
        CompletableFuture.allOf(buyOrdersFuture, sellOrdersFuture)
                .thenAccept(ignored -> {
                    // Populate the matching engine
                	matchingService.initializeBuyOrderQueue(buyOrdersFuture.join());
                	matchingService.initializeSellOrderQueue(sellOrdersFuture.join());
                });
        
		log.info("Completed initializing Order Queue");

	}
	
}
