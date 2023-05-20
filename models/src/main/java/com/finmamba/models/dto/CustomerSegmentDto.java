package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSegmentDto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String segmentName;

	private String segmentDescription;
}
