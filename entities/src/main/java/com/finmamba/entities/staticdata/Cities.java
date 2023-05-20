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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "cities",
        schema = "static"
)
@Entity
public class Cities implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
	
	@Column(name="name")
	private String name;
	
	@OneToOne
	@JoinColumn(name="state_id", referencedColumnName = "id")
	private States states;
}
