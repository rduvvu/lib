package com.jbhunt.edi.sterlingarchive.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Data
@Configuration
@Profile("LOCAL")
public class ActiveMqProperties {
    @Value("${messaging.activeMq.username}")
    private String username;
    @Value("${messaging.activeMq.password}")
    private String password;

    @Value("${messaging.activeMq.brokerUrl}")
    private String consumerBrokerUrl;

    @Value("${messaging.activeMq.brokerUrl}")
    private String producerBrokerUrl;

    @Value("${jbhunt.general.jms.activeMQ.connectionFactory.edi.consumer.maxConnections}")
    private int consumerMaxConnections;

    @Value("${jbhunt.general.jms.activeMQ.connectionFactory.edi.producer.maxConnections}")
    private int producerMaxConnections;


}
