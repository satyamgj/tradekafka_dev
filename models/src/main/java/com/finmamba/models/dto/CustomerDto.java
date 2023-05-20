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
public class CustomerDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String nickname;

	private String firstName;

	private String middleName;

	private String lastName;

	private String email;

	private CountryDto country;

	private long phone;

	private String password;

	private String address;

	private String salt;

	private String oAuthKey;

	private UserDto userId;

	//private CustomerSegmentDto customerSegment;

	private String oAuthIdp;

	private String favourites;
}
