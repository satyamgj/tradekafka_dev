package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TenorDto implements Serializable{
	private long id;
	
	private String tenorUnit;
}
