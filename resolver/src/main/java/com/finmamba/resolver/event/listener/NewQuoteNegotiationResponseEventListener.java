package com.finmamba.resolver.event.listener;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.entities.staticdata.EventType;
import com.finmamba.entities.transaction.KanjiWorld;
import com.finmamba.entities.transaction.KotakWorld;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.KanjiWorldDto;
import com.finmamba.models.dto.KotakWorldDto;
import com.finmamba.resolver.event.NewNegotiationResponse;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.DaddyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.KanjiWorldRepository;
import com.finmamba.resolver.repositories.KotakWorldRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.TenorRepository;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewQuoteNegotiationResponseEventListener implements ApplicationListener<NewNegotiationResponse> {

	@Autowired
	KotakWorldRepository kotakWorldRepository;

	@Autowired
	KanjiWorldRepository kanjiWorldRepository;

	@Autowired
	DaddyRepository daddyRepository;

	@Autowired
	EventTypeRepository eventTypeRepository;

	@Autowired
	InstitutionalEntityRepository institutionalEntityRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	TransactionServiceDal dal;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	DirectionRepository directionRepository;

	@Autowired
	TenorRepository tenorRepository;

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	Gson gson;

	@Override
	public void onApplicationEvent(NewNegotiationResponse event) {

		log.info("Inside NewNegotiationResponse");
		KotakWorldDto kotakWorldDto = event.getKotakWorldDto();
		log.info(kotakWorldDto.toString());
		
		int responseType = event.getResponseType();

		KotakWorldDto reponseDto = null;
		KotakWorld response = null;
		// case to decide whether the response is a confirm order of a negotiation

		if(responseType == 1) { //DEAL
			
			EventType inNegotiationEvent = eventTypeRepository.getById(5L);
			KotakWorld inNegotiation = kotakWorldRepository
					.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive
					(kotakWorldDto.getTransactionId(), institutionalEntityRepository.getById(1L), inNegotiationEvent,true);
			inNegotiation.setRequestActive(false);
			kotakWorldRepository.save(inNegotiation);
			
			KotakWorld kotakWorld = KotakWorld.builder().transactionId(kotakWorldDto.getTransactionId())
					.source(institutionalEntityRepository.findById(1L).get())
					.baseCurrency(currencyRepository.getById(kotakWorldDto.getBaseCurrency().getId()))
					.customer(customerRepository.getById(kotakWorldDto.getCustomer().getId()))
					.quoteCurrency(currencyRepository.getById(kotakWorldDto.getQuoteCurrency().getId()))
					.product(productRepository.getById(kotakWorldDto.getProduct().getId()))
					.buySellDirection(directionRepository.getById(kotakWorldDto.getBuySellDirection().getId()))
					.notional(kotakWorldDto.getNotional()).sourceSpotRate(kotakWorldDto.getSourceSpotRate())
					.sourceSpotSpread(kotakWorldDto.getSourceSpotSpread())
					.destinationSpotRate(kotakWorldDto.getDestinationSpotRate())
					.sourceSwapPoints(kotakWorldDto.getSourceSwapPoints())
					.destinationSwapRate(kotakWorldDto.getDestinationSwapRate())
					.sourceOutright(kotakWorldDto.getSourceOutright()).spreadOutright(kotakWorldDto.getSpreadOutright())
					.destinationOutright(kotakWorldDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
					.isRequestActive(false)
					.destinationId(institutionalEntityRepository.getById(3L)) // get from entity_communication_map
					.eventType(eventTypeRepository.getById(4L)) //OUT_RESPONSE															// table
					.build();
			
			kotakWorldRepository.save(kotakWorld);
			
			
			// deavtivate the request for this response
			EventType outNegotiationEvent = eventTypeRepository.getById(6L);
			KanjiWorld outRequest = kanjiWorldRepository
					.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive
					(kotakWorldDto.getTransactionId(), null, outNegotiationEvent,true);
			outRequest.setRequestActive(false);
			kanjiWorldRepository.save(outRequest);

			// Insert record for request initiator
			KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(kotakWorldDto.getTransactionId())
					.source(institutionalEntityRepository.findById(1L).get()) // SELF
					.baseCurrency(currencyRepository.getById(kotakWorldDto.getBaseCurrency().getId()))
					.customer(customerRepository.getById(kotakWorldDto.getCustomer().getId()))
					.quoteCurrency(currencyRepository.getById(kotakWorldDto.getQuoteCurrency().getId()))
					.product(productRepository.getById(kotakWorldDto.getProduct().getId()))
					.buySellDirection(directionRepository.getById(kotakWorldDto.getBuySellDirection().getId()))
					.notional(kotakWorldDto.getNotional()).sourceSpotRate(kotakWorldDto.getSourceSpotRate())
					.sourceSpotSpread(kotakWorldDto.getSourceSpotSpread())
					.destinationSpotRate(kotakWorldDto.getDestinationSpotRate())
					.sourceSwapPoints(kotakWorldDto.getSourceSwapPoints())
					.destinationSwapRate(kotakWorldDto.getDestinationSwapRate())
					.sourceOutright(kotakWorldDto.getSourceOutright()).spreadOutright(kotakWorldDto.getSpreadOutright())
					.destinationOutright(kotakWorldDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
					.isRequestActive(true)
					.destinationId(institutionalEntityRepository.getById(3L)) // get from entity_communication_map
					.eventType(eventTypeRepository.getById(3L)) // in response																	// table
					.build();
			KanjiWorld savedResponse = kanjiWorldRepository.save(kanjiWorld);			
			
			String kafkaTopic = "KanjiWorld" + "." + "FXC" + "." + "MumbaiEntity" + "." + "spot-desk";

			log.info("Pushing on kafka topic: " + kafkaTopic);
			
			KanjiWorldDto saveddto = modelMapper.map(savedResponse, KanjiWorldDto.class);
			List<KanjiWorldDto> listSavedQuote = new ArrayList<>();
			listSavedQuote.add(saveddto);
			kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);
		}
		//NEGOTIATION
		else if(responseType == 2)
			{ 
			
				EventType inNegotiationEvent = eventTypeRepository.getById(5L);
				KotakWorld inNegotiation = kotakWorldRepository
						.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive
						(kotakWorldDto.getTransactionId(), institutionalEntityRepository.getById(1L), inNegotiationEvent,true);
				inNegotiation.setRequestActive(false);
				kotakWorldRepository.save(inNegotiation);
				
				KotakWorld kotakWorld = KotakWorld.builder().transactionId(kotakWorldDto.getTransactionId())
						.source(institutionalEntityRepository.findById(1L).get())
						.baseCurrency(currencyRepository.getById(kotakWorldDto.getBaseCurrency().getId()))
						.customer(customerRepository.getById(kotakWorldDto.getCustomer().getId()))
						.quoteCurrency(currencyRepository.getById(kotakWorldDto.getQuoteCurrency().getId()))
						.product(productRepository.getById(kotakWorldDto.getProduct().getId()))
						.buySellDirection(directionRepository.getById(kotakWorldDto.getBuySellDirection().getId()))
						.notional(kotakWorldDto.getNotional()).sourceSpotRate(kotakWorldDto.getSourceSpotRate())
						.sourceSpotSpread(kotakWorldDto.getSourceSpotSpread())
						.destinationSpotRate(kotakWorldDto.getDestinationSpotRate())
						.sourceSwapPoints(kotakWorldDto.getSourceSwapPoints())
						.destinationSwapRate(kotakWorldDto.getDestinationSwapRate())
						.sourceOutright(kotakWorldDto.getSourceOutright()).spreadOutright(kotakWorldDto.getSpreadOutright())
						.destinationOutright(kotakWorldDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
						.isRequestActive(kotakWorldDto.isRequestActive())
						.destinationId(institutionalEntityRepository.getById(3L)) // get from entity_communication_map
						.eventType(eventTypeRepository.getById(6L)) //OUT_NEGOTIATION															// table
						.build();
				
				kotakWorldRepository.save(kotakWorld);
				
				
				// deavtivate the request for this response
				EventType outNegotiationEvent = eventTypeRepository.getById(6L);
				KanjiWorld outNegotiation = kanjiWorldRepository.findByTransactionIdAndDestinationIdAndEventTypeAndIsRequestActive(
						kotakWorldDto.getTransactionId(), null, outNegotiationEvent,true);
				outNegotiation.setRequestActive(false);
				kanjiWorldRepository.save(outNegotiation);

				// Insert record for request initiator
				KanjiWorld kanjiWorld = KanjiWorld.builder().transactionId(kotakWorldDto.getTransactionId())
						.source(institutionalEntityRepository.findById(1L).get()) // SELF
						.baseCurrency(currencyRepository.getById(kotakWorldDto.getBaseCurrency().getId()))
						.customer(customerRepository.getById(kotakWorldDto.getCustomer().getId()))
						.quoteCurrency(currencyRepository.getById(kotakWorldDto.getQuoteCurrency().getId()))
						.product(productRepository.getById(kotakWorldDto.getProduct().getId()))
						.buySellDirection(directionRepository.getById(kotakWorldDto.getBuySellDirection().getId()))
						.notional(kotakWorldDto.getNotional()).sourceSpotRate(kotakWorldDto.getSourceSpotRate())
						.sourceSpotSpread(kotakWorldDto.getSourceSpotSpread())
						.destinationSpotRate(kotakWorldDto.getDestinationSpotRate())
						.sourceSwapPoints(kotakWorldDto.getSourceSwapPoints())
						.destinationSwapRate(kotakWorldDto.getDestinationSwapRate())
						.sourceOutright(kotakWorldDto.getSourceOutright()).spreadOutright(kotakWorldDto.getSpreadOutright())
						.destinationOutright(kotakWorldDto.getDestinationOutright()).tenorUnit(tenorRepository.getById(1L))
						.isRequestActive(kotakWorldDto.isRequestActive())
						.destinationId(institutionalEntityRepository.getById(3L)) // get from entity_communication_map
						.eventType(eventTypeRepository.getById(5L)) // IN_NEGOTIATION																	// table
						.build();
				KanjiWorld savedResponse = kanjiWorldRepository.save(kanjiWorld);			
				
				String kafkaTopic = "KanjiWorld" + "." + "FXC" + "." + "MumbaiEntity" + "." + "spot-desk";

				log.info("Pushing on kafka topic: " + kafkaTopic);
				
				KanjiWorldDto saveddto = modelMapper.map(savedResponse, KanjiWorldDto.class);
				List<KanjiWorldDto> listSavedQuote = new ArrayList<>();
				listSavedQuote.add(saveddto);
				kafkaProducerService.send(listSavedQuote, kafkaTopic, null, null);
			
		}
	}
}
