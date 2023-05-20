package com.finmamba.entities.dblistener;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import com.finmamba.entities.transaction.IciciWorld;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IciciWorldTableListener {
	
	@Autowired
	KafkaTemplate<Integer, Message<Object>> kafkaTemplateMessageObject;
	
	SendOnKafka sendOnKafka = new SendOnKafka();
	
	String topic = "notification-service-request";
	
	@Autowired
	ModelMapper modelMapper;

	@PostPersist
	public void insertIciciWorldTable(IciciWorld iciciWorld) {
		log.info(iciciWorld.getTransactionId());
		sendOnKafka.send( modelMapper.map(iciciWorld, SuperInstitutionalEntityDto.class) , topic,
				"".getBytes(), null, kafkaTemplateMessageObject);
	}
	
	@PostUpdate
	public void updateIciciWorldTable(IciciWorld iciciWorld) {
		log.info("Updated" + iciciWorld.toString());
	}
	
}