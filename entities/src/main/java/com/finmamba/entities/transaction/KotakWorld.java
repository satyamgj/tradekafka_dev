package com.finmamba.entities.transaction;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import com.finmamba.entities.dblistener.KotakWorldTableListener;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EntityListeners(KotakWorldTableListener.class)
@Entity
@Table(name = "kotakworld", schema = "transaction")
@NoArgsConstructor
@SuperBuilder
public class KotakWorld extends SuperInstitutionalEntity {
	private static final long serialVersionUID = 1L;
	
}
