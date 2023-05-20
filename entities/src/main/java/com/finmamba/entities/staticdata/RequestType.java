package com.finmamba.entities.staticdata;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "request_type", schema = "static")
@Data
public class RequestType implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "short_name")
	private String shortName;
	
	@Column(name = "request_name")
	private String requestName;
	
	@Column(name = "request_description")
	private String requestDescription;
	
}
