package com.finmamba.resolver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.transaction.Customer;
import com.finmamba.entities.transaction.User;


@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>{
	public List<Customer> findByUserId(User user);
}
