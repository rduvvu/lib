package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.edi.sterlingarchive.properties.ActiveMqProperties;

import org.apache.camel.component.activemq.ActiveMQComponent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class ActiveMqConfigurationTest {
    @InjectMocks
    ActiveMqConfiguration activeMqConfiguration;
    @Mock
    ActiveMqProperties activeMqProperties;
    @Mock
    ActiveMQComponent activeMQComponent;
    @Mock
    PIDCredentials pidCredentials;

    @Before
    public void  before(){
        activeMqConfiguration  = new ActiveMqConfiguration(pidCredentials,activeMqProperties);
    }

    @Test
    public void testActiveAmqConsumer(){
        try {
            Mockito.when(activeMqProperties.getConsumerBrokerUrl()).thenReturn("www.google.com");
            Mockito.when(activeMqProperties.getConsumerMaxConnections()).thenReturn(1);

            ActiveMQComponent activeMQComponent = activeMqConfiguration.activeAmqConsumer();
          Assert.assertNotNull(activeMQComponent.getConfiguration());
          Assert.assertEquals("www.google.com", activeMqProperties.getConsumerBrokerUrl());
        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testActiveMQProducer(){
        try {
            Mockito.when(activeMqProperties.getProducerBrokerUrl()).thenReturn("www.google.com");
            Mockito.when(activeMqProperties.getProducerMaxConnections()).thenReturn(1);

            ActiveMQComponent activeMQComponent = activeMqConfiguration.activeMQProducer();
            Assert.assertNotNull(activeMQComponent.getConfiguration());
            Assert.assertEquals("www.google.com", activeMqProperties.getProducerBrokerUrl());
        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

}
