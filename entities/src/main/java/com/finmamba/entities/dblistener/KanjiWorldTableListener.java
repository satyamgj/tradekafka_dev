package com.finmamba.entities.dblistener;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import com.finmamba.entities.transaction.KanjiWorld;
import com.finmamba.models.dto.SuperInstitutionalEntityDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KanjiWorldTableListener {
	
	@Autowired
	KafkaTemplate<Integer, Message<Object>> kafkaTemplateMessageObject;
	
	SendOnKafka sendOnKafka = new SendOnKafka();
	
	String topic = "notification-service-request";
	
	
	@Autowired
	ModelMapper modelMapper;

	@PostPersist
	public void insertKanjiWorldTable(KanjiWorld kanjiWorld) {
		log.info(kanjiWorld.getTransactionId());
		sendOnKafka.send( modelMapper.map(kanjiWorld, SuperInstitutionalEntityDto.class) , topic,
				"".getBytes(), null, kafkaTemplateMessageObject);
	}
	
	@PostUpdate
	public void updateKanjiWorldTable(KanjiWorld kanjiWorld) {
		log.info("Updated" + kanjiWorld.toString());
	}
	
	
	
}
