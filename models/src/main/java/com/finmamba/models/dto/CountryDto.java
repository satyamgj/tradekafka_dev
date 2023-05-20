package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryDto implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private Long id;

    private String iso;
    
    private String iso3;
    
    private int isoNumeric;
    
    private String name;
    
    private String capital;
    
    private String continentCode;
    
    private CurrencyDto currency;

}
