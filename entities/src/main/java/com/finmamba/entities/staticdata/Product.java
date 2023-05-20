package com.finmamba.entities.staticdata;

import java.io.Serializable;

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
@Table(name = "products", schema = "static")
@Data
public class Product implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "name", nullable = false, insertable = false, updatable = false, unique = true)
	private String name;
	
	@OneToOne
	@JoinColumn(name = "asset_class_id")
	private AssetClass assetClass;
}
