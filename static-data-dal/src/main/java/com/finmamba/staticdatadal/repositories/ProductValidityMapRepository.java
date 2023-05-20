package com.finmamba.staticdatadal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.ProductValidityMap;

@Repository
public interface ProductValidityMapRepository extends JpaRepository<ProductValidityMap, Long>{

}
