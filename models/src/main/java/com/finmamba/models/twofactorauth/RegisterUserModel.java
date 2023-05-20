package com.finmamba.models.twofactorauth;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class RegisterUserModel {
	@NonNull
	private String emailAddress;
	
	@NonNull
	private long userRoleId;
}
