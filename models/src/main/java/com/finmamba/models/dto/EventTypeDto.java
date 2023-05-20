package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class EventTypeDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	
	private String eventName;
	
	private String requestDescription;
	
}
