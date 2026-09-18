package com.starbank.recommendation.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import liquibase.integration.spring.SpringLiquibase;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "rulesDataSource")
    @ConfigurationProperties(prefix = "spring.rules.datasource")
    public DataSource rulesDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SpringLiquibase rulesLiquibase(@Qualifier("rulesDataSource") DataSource rulesDataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(rulesDataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        return liquibase;
    }
}
