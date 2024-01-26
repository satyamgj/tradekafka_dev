package com.tradekafka.ondc.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.finmamba.entities.staticdata.Direction;

@Service
public class DirectionStaticData {

	Map<Long, Direction>directionMap = new HashMap<>();
	
	public Map<Long, Direction> getDirectionMap(){
		return directionMap;
	}
	
	public void addDirection(Direction direction) {
		directionMap.put(direction.getId(), direction);
	}
}
