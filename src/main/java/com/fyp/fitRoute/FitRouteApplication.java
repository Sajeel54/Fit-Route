package com.fyp.fitRoute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class FitRouteApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitRouteApplication.class, args);
	}

	@Bean
	public PlatformTransactionManager tcl(MongoDatabaseFactory dbFactory){
		return new MongoTransactionManager(dbFactory);
	}

}
