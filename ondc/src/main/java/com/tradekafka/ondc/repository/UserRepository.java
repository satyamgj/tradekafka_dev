package com.tradekafka.ondc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	public User getByemailId(String userEmail);
	
}
