package com.finmamba.resolver.operations;

import java.time.Instant;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finmamba.entities.transaction.CounterpartyTransactionDetails;
import com.finmamba.entities.transaction.KotakWorld;
import com.finmamba.kafka.starter.autoconfigure.producer.KafkaProducerService;
import com.finmamba.models.dto.KotakWorldDto;
import com.finmamba.models.dto.QuoteRequestDto;
import com.finmamba.resolver.repositories.CurrencyRepository;
import com.finmamba.resolver.repositories.CustomerRepository;
import com.finmamba.resolver.repositories.DaddyRepository;
import com.finmamba.resolver.repositories.DirectionRepository;
import com.finmamba.resolver.repositories.EventTypeRepository;
import com.finmamba.resolver.repositories.InstitutionalEntityRepository;
import com.finmamba.resolver.repositories.KotakWorldRepository;
import com.finmamba.resolver.repositories.ProductRepository;
import com.finmamba.resolver.repositories.QuoteRequestRepository;
import com.finmamba.resolver.repositories.TenorRepository;
import com.finmamba.resolver.repositories.UserRepository;
import com.finmamba.resolver.services.EventPublisher;
import com.finmamba.resolver.services.TransactionServiceDal;
import com.finmamba.resolver.util.ApplicationConstants;

import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@Builder
public class CounterPartyOperation implements Operation{
	
	@Autowired
	KotakWorldRepository kotakWorldRepository;

	@Autowired
	DaddyRepository daddyRepository;
	
	@Autowired
	QuoteRequestRepository quoteRequestRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	DirectionRepository directionRepository;

	@Autowired
	EventTypeRepository eventTypeRepository;

	@Autowired
	TenorRepository tenorRepository;

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
	CustomerRepository customerRepository;

	@Autowired
	InstitutionalEntityResolver entityResolver;

	@Autowired
	KafkaProducerService kafkaProducerService;
	
	@Autowired
	UserRepository userRepository;

	/**
	 *@apiNote : inserts in request in CounterPartyTransactionTable 
	 **/
	@Override
	public QuoteRequestDto insertQuoteRequest(QuoteRequestDto quoteRequestDto) {
		
		log.info("Inserting quote request started for " + quoteRequestDto.getSource().getRealmName() + " with transactionId: "
				+ quoteRequestDto.getTransactionId());

		CounterpartyTransactionDetails transaction = CounterpartyTransactionDetails.builder()
				.transactionId(quoteRequestDto.getTransactionId())
				.sourceOutright(quoteRequestDto.getSourceOutright()).spreadOutright(quoteRequestDto.getSpreadOutright())
				.destinationOutright(quoteRequestDto.getDestinationOutright()).isRequestActive(false)
				.destinationId(institutionalEntityRepository.getById(daddyDto.getSource().getId())) // SELF
				.tenorUnit(tenorRepository.getById(1L))
				.eventType(eventTypeRepository.getById(ApplicationConstants.Event.IN_REQUEST.getEventId()))
				.createdAt(Instant.now())
				.createdBy(userRepository.getById(Long.valueOf(daddyDto.getCreatedBy().getId())))
				.build();
		KotakWorld kotakWorldSaved = kotakWorldRepository.save(kotakWorld);

		KotakWorldDto kotakWorldDto = modelMapper.map(kotakWorldSaved, KotakWorldDto.class);
		kotakWorldDto.setSource(daddyDto.getSource());

		log.info("insertDaddyRequest ended for " + daddyDto.getSource().getRealmName());

		return kotakWorldDto;
	}

}
