package com.dnastack.ddap;

import com.dnastack.ddap.explore.config.model.AppConfig;
import com.dnastack.ddap.ic.common.config.IdpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties(value = {AppConfig.class, IdpProperties.class })
@SpringBootApplication
public class DdapExploreApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdapExploreApplication.class, args);
	}

}

