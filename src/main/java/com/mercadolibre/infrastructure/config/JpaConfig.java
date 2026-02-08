package com.mercadolibre.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EntityScan("com.mercadolibre.infrastructure.adapter.output.persistence.entity")
@EnableJpaRepositories("com.mercadolibre.infrastructure.adapter.output.persistence")
@EnableTransactionManagement(proxyTargetClass = true)
public class JpaConfig {
}
