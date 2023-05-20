

package com.finmamba.entities.dblistener;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import com.finmamba.entities.transaction.KotakWorld;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KotakWorldTableListener {
	
	@Autowired
	KafkaTemplate<Integer, Message<Object>> kafkaTemplateMessageObject;
	
	SendOnKafka sendOnKafka = new SendOnKafka();
	
	String topic = "notification-service-request";
	
	@Autowired
	ModelMapper modelMapper;

	@PostPersist
	public void insertKotakWorldTable(KotakWorld kotakWorld) {
		log.info(kotakWorld.getTransactionId());
		sendOnKafka.send( modelMapper.map(kotakWorld, SuperInstitutionalEntityDto.class) , topic,
				"".getBytes(), null, kafkaTemplateMessageObject);
	}
	
	@PostUpdate
	public void updateKotakWorldTable(KotakWorld kotakWorld) {
		log.info("Updated" + kotakWorld.toString());
	}
	
}