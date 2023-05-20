package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class GeographicalEntityDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	
	private String entityName;

	private String entityDescription;
}
