package com.finmamba.resolver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finmamba.resolver.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("getId/{userEmail}")
	public ResponseEntity<Object> getUserId(@PathVariable("userEmail") String userEmail) {
		
		
		return new ResponseEntity<>(userRepository.getByemailId(userEmail), HttpStatus.OK);
	}
	
}
