package com.finmamba.staticdatadal.configuration;

import org.springframework.context.annotation.Configuration;



import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


@Configuration
public class RedisConfig {

    //Creating Connection with Redis
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    //Creating RedisTemplate for Entity 'Employee'
    @Bean
    public RedisTemplate<String, Object > redisTemplate(){
        RedisTemplate<String, Object> empTemplate = new RedisTemplate<>();
        empTemplate.setConnectionFactory(redisConnectionFactory());
        return empTemplate;
    }
	
}
