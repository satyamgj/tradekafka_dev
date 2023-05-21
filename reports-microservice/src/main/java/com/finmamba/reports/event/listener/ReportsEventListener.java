package com.finmamba.reports.event.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.finmamba.reports.event.ReportGenerationSimulation;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReportsEventListener implements ApplicationListener<ReportGenerationSimulation> {
	@Override
	public void onApplicationEvent(ReportGenerationSimulation event) {
		// TODO Auto-generated method stub
		
	}
	/*

	@Autowired
	KafkaProducerService kafkaProducerService;

	@Autowired
	Gson gson;

	@Autowired
	ReportsOperationsServiceDal simulatorOperationsServiceDal;

	@Autowired
	RateSimulatorCalculator rateSimulatorCalculator;

	@Value("${stream-interval}")
	long streamInterval;
	
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
	
	

	@Override
	public void onApplicationEvent(StartRateSimulation event) {
		//List<CurrencyPairRates> ccyPairRates = simulatorOperationsServiceDal.getCurrencyPairSpotRates();
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		
		
		Map<String, CurrencyPairRatesDto> prevCopy = new HashMap<>();
		
		Map<String, List<CurrencyPairRatesDto>> mapList = new HashMap<>();
		
		

		while (true) {
			Map<String, CurrencyPairRatesDto> map = new HashMap<>();
			List<CurrencyPairRatesDto> listMap = new ArrayList<>();
			
			ccyPairRates.forEach(pair -> {

				log.debug("######Currency pair: " + pair.getBaseCurrency() + "/" + pair.getQuoteCurrency() + "######");
				log.debug("######Currency pair: " + pair.getBaseCurrency() + "/" + pair.getQuoteCurrency() + "######");
				boolean direction = (Math.random() < 0.5);
				
				double adjustedValueBid;
				double adjustedValueAsk;
				
				double finalBidValue = 0;
				double finalAskValue = 0;
				Pair<Double, Double> randomNumBidAsk = null;
				do {
					randomNumBidAsk = rateSimulatorCalculator.spotRateAdjustmentCalculator(
							pair.getBidPrice().doubleValue(), pair.getAskPrice().doubleValue(), 0);
					log.debug("direction " + direction);
					if (direction) {
						adjustedValueBid = randomNumBidAsk.getFirst();
						adjustedValueAsk = randomNumBidAsk.getSecond();
					} else {
						adjustedValueBid = -randomNumBidAsk.getFirst();
						adjustedValueAsk = -randomNumBidAsk.getSecond();
					}
					finalBidValue = pair.getBidPrice().doubleValue() + adjustedValueBid * 0.01;
					finalAskValue = pair.getAskPrice().doubleValue() + adjustedValueAsk * 0.01;
				}while(finalBidValue > finalAskValue);
				
				log.debug("final Bid Value " + finalBidValue);
				log.debug("final Ask Value " + finalAskValue);
				
				String ccyPair = pair.getBaseCurrency() + "/" + pair.getQuoteCurrency();
				
				//@// @formatter:off
				
				CurrencyDto buyCcyDto = CurrencyDto.builder()
						.id(pair.getBuyCurrencyId())
						.isoCode(pair.getBaseCurrency())
						.build();
				
				CurrencyDto sellCcyDto = CurrencyDto.builder()
						.id(pair.getSellCurrencyId())
						.isoCode(pair.getQuoteCurrency())
						.build();
				
				CurrencyPairRatesDto dto = CurrencyPairRatesDto.builder()
						.ccyPair(ccyPair)
						.buyCurrency(buyCcyDto)
						.sellCurrency(sellCcyDto)
						.bidPrice(finalBidValue)
						.askPrice(finalAskValue)
						.build();
				// @formatter:on
				CurrencyPairRatesDto prevDto = prevCopy.get(ccyPair);
				
				if(prevDto != null)
				{
					double bidChange = dto.getBidPrice() - prevDto.getBidPrice();
					double askChange = dto.getAskPrice() - prevDto.getAskPrice();
					
					double percBidChange = adjustedValueBid;
							//bidChange / prevDto.getBidPrice();
					double percAskChange = adjustedValueAsk;
					//askChange / prevDto.getAskPrice();
					
					dto.setBidChange(Math.round(bidChange * 10000.0) / 10000.0);
					dto.setAskChange(Math.round(askChange * 10000.0) / 10000.0);
					
					dto.setAskPercentChange(Math.round(percAskChange * 10000.0) / 10000.0);
					dto.setBidPercentChange(Math.round(percBidChange * 10000.0) / 10000.0);
					
				}
				
//				Type mapCcyPair = new TypeToken<Map<String, CurrencyPairRatesDto>>() {
//				}.getType();
				map.put(ccyPair, dto);
				
				listMap.add(dto);
				log.debug("############");
			});
			
			log.debug(gson.toJson(map));
			mapList.put("data", listMap);
			log.info(gson.toJson(mapList));
			kafkaProducerService.send(gson.toJson(mapList), "rate-stream", null, null);
			log.info("produced on kafka");
			prevCopy.clear();
			prevCopy.putAll(map);
			try {
				Thread.sleep(streamInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

*/}
