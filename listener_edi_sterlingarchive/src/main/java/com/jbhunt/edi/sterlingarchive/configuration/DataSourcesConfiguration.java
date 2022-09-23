package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.biz.securepid.PIDCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * This configuration class configures the persistence layer to our application and
 * enables annotation driven transaction management.
 */
@Slf4j
@Configuration
public class DataSourcesConfiguration {

    private final PIDCredentials pidCredentials;

    public DataSourcesConfiguration(PIDCredentials pidCredentials){
        this.pidCredentials = pidCredentials;
    }

    @Bean(name = "ediNewDataSource")
    @Primary
    @ConfigurationProperties(prefix = "jbhunt.b2b.edidatasource.electronicdatainterchange")
    public DataSource ediNewDataSource(@Value("${jbhunt.b2b.edidatasource.electronicdatainterchange.url}") String url) {
        log.info("Creating edi sql datasource with url " + url);
        return DataSourceBuilder.create()
                .username(pidCredentials.getUsername())
                .password(pidCredentials.getPassword())
                .url(url)
                .build();
    }

    @Bean
    public NamedParameterJdbcTemplate ediJdbcTemplate(
            @Qualifier("ediNewDataSource") DataSource ediDataSource) {
        return new NamedParameterJdbcTemplate(ediDataSource);
    }
}
