package com.finmamba.entities.staticdata.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.finmamba.entities.staticdata.configuration.ImportEntitiesConfiguration;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ImportEntitiesConfiguration.class)
public @interface ImportEntities {

}
