package com.finmamba.entities.staticdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer_segment", schema = "static")
@Entity
public class CustomerSegment implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "segment_name")
	private String segmentName;

	@Column(name = "segment_description")
	private String segmentDescription;
}
