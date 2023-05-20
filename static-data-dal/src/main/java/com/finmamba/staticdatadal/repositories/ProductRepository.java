
package com.finmamba.staticdatadal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finmamba.entities.staticdata.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

}
