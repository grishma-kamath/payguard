package com.grishma.payguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class PayguardApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(PayguardApplication.class, args);
	}
}

//Hibernate: create table transactions (id varchar(255) not null, account_id varchar(255), amount float(53) not null, assessed_at timestamp(6), blocked boolean not null, flag varchar(255), message varchar(255), risk_level varchar(255), transaction_id varchar(255), type varchar(255), primary key (id))
//Hibernate automatically created the transactions table in PostgreSQL! You didn't write a single line of SQL. That's the power of JPA.
//And then:
//HikariPool-1 - Start completed.
//Started PayguardApplication in 13.976 seconds
//Spring Boot is connected to PostgreSQL. The app is running.