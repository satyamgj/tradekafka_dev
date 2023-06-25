package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectionDto implements Serializable{
	private static final long serialVersionUID = 1L;
	private Long id;
	private String name;
}
