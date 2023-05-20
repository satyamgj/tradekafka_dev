package com.finmamba.models.transaction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserData {
  
	private Long id;
	private String firstName;
	private String middleName;
	private String lastName;
	private long userRoleId;
	private String emailId;
	private long countryId;
	private long phone;
	private String password;
	private String address;
	private String passwordSalt;
	private String userOAuthKey;
	private String userOAuthIdp;
	private long entityId;
	private String favourites;
}
