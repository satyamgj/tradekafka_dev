package com.finmamba.entities.staticdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "countries",
        schema = "static"
)
@Entity
public class Country implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="iso")
    private String iso;
    
    @Column(name="iso3")
    private String iso3;
    
    
    @Column(name="name")
    private String name;


}
