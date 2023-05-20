package com.finmamba.entities.staticdata.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {"com.finmamba.entities"})
public class ImportEntitiesConfiguration {

}
