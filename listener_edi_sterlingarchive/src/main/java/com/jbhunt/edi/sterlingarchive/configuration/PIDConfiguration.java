package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.biz.securepid.PIDCredentials;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class PIDConfiguration {

    @Value("${DOMAIN_PID}")
    private String userID;

    @Value("${DOMAIN_PASSWORD}")
    private String password;

    @Bean
    public com.jbhunt.biz.securepid.PIDCredentials pidCredentials() {
        return new PIDCredentials(userID, password);
    }
}
