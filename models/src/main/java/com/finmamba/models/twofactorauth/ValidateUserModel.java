package com.finmamba.models.twofactorauth;

import org.springframework.lang.NonNull;

import lombok.Data;

@Data
public class ValidateUserModel {
	@NonNull
	private String emailAddress;
	
	@NonNull
	private int verificationCode;
}
