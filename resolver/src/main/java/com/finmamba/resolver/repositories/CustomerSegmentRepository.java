package com.finmamba.resolver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.CustomerSegment;

@Repository
public interface CustomerSegmentRepository extends JpaRepository<CustomerSegment, Long>{

}
