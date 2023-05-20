package com.finmamba.entities.staticdata;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "timer_units", schema = "static")
@Data
public class TimerUnit implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name = "unit_name", nullable = false, insertable = false, updatable = false, unique = true)
	private String unitName;
	
	@Column(name = "unit_precision", nullable = false, insertable = false, updatable = false, unique = true)
	private BigDecimal unitPresicion;
}
