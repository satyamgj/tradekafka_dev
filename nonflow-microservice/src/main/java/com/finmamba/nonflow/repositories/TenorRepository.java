package com.finmamba.nonflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Tenor;

@Repository
public interface TenorRepository extends JpaRepository<Tenor, Long>{

}
