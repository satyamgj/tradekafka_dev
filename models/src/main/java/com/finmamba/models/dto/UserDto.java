package com.finmamba.models.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserDto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	private String firstName;
	private String middleName;
	private String lastName;
	private String emailId;
	private CountryDto country;
	private long phone;
	private String password;
	private String address;
	private String passwordSalt;
	private String userOAuthKey;
	private String userOAuthIdp;
	private GeographicalEntityDto entity;
	private String favourites;

}
