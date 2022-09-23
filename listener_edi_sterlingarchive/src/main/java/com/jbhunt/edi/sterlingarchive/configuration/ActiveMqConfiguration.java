package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.edi.sterlingarchive.constants.SterlingArchiveConstants;
import com.jbhunt.edi.sterlingarchive.properties.ActiveMqProperties;
import org.apache.activemq.ActiveMQConnectionFactory;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.component.activemq.ActiveMQConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(SterlingArchiveConstants.SPRING_LOCAL_PROFILE)
public class ActiveMqConfiguration {

    private final PIDCredentials pidCredentials;
    private final ActiveMqProperties activeMqProperties;

    public ActiveMqConfiguration(PIDCredentials pidCredentials, ActiveMqProperties activeMqProperties) {
        this.pidCredentials = pidCredentials;
        this.activeMqProperties = activeMqProperties;
    }

    @Bean
    public ActiveMQComponent activeAmqConsumer() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(activeMqProperties.getConsumerBrokerUrl());
        activeMQConnectionFactory.setUserName(activeMqProperties.getUsername());
        activeMQConnectionFactory.setPassword(activeMqProperties.getPassword());

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(activeMqProperties.getConsumerMaxConnections());

        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        activeMQConfiguration.setBrokerURL(activeMqProperties.getConsumerBrokerUrl());

        return new ActiveMQComponent(activeMQConfiguration);
    }

    @Bean
    public ActiveMQComponent activeMQProducer() {
        // Connection Factory
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(activeMqProperties.getProducerBrokerUrl());
        activeMQConnectionFactory.setUserName(activeMqProperties.getUsername());
        activeMQConnectionFactory.setPassword(activeMqProperties.getPassword());

        // Pooled Connection Factory
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(activeMqProperties.getProducerMaxConnections());

        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        activeMQConfiguration.setBrokerURL(activeMqProperties.getProducerBrokerUrl());

        return new ActiveMQComponent(activeMQConfiguration);
    }
}
