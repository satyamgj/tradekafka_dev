package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class AssetClassDto implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

    private String name;

}
