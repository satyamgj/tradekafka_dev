package com.finmamba.entities.staticdata;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "product_validity_map", schema = "static")
@Data
public class ProductValidityMap implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne
	@JoinColumn(name = "product_id")
	private Product product;
	
	@Column(name = "timer_value")
	private BigDecimal timerValue;
	
	@Column(name = "timer_id")
	private int timerid;
	
}
