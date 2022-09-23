package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.edi.sterlingarchive.properties.WebMQProperties;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;

@RunWith(SpringJUnit4ClassRunner.class)
public class WebMqConfigurationTest {
    @InjectMocks
    WebMqConfiguration webMqConfiguration;

    @Test
    public void testConsumerConnectionFactory(){
        WebMQProperties webMQProperties =new WebMQProperties();
        webMQProperties.setArchiveHost("test");
        webMQProperties.setArchiveChannel("test");
        webMQProperties.setArchivePort(5847);
        webMQProperties.setArchiveQueueManager("TestManager");
        WebMqConfiguration webMqConfiguration = new WebMqConfiguration(webMQProperties);
        ConnectionFactory connectionFactory= webMqConfiguration.consumerConnectionFactory();
        Assert.assertNotNull(connectionFactory);
    }

    @Test(expected = RuntimeException.class)
    public void testConsumerConnectionFactoryException(){
        WebMQProperties webMQProperties =new WebMQProperties();
        webMQProperties.setArchiveHost("test");
        webMQProperties.setArchiveChannel("test");
        webMQProperties.setArchivePort(5847);
        webMQProperties.setArchiveQueueManager("TestManager");
        Mockito.when(webMqConfiguration.consumerConnectionFactory()).thenThrow(new RuntimeException());
        ConnectionFactory connectionFactory= webMqConfiguration.consumerConnectionFactory();
        Assert.assertNotNull(connectionFactory);
    }

    @Test
    public void testActiveMQProducer(){
        WebMQProperties webMQProperties =new WebMQProperties();
        webMQProperties.setErrorHost("test");
        webMQProperties.setErrorChannel("test");
        webMQProperties.setErrorPort(5847);
        webMQProperties.setErrorQueueManager("TestManager");
        WebMqConfiguration webMqConfiguration = new WebMqConfiguration(webMQProperties);
        ConnectionFactory connectionFactory= webMqConfiguration.producerConnectionFactory();
        Assert.assertNotNull(connectionFactory);
    }

    @Test(expected = RuntimeException.class)
    public void testActiveMQProducerException(){
        WebMQProperties webMQProperties =new WebMQProperties();
        webMQProperties.setErrorHost("test");
        webMQProperties.setErrorChannel("test");
        webMQProperties.setErrorPort(5847);
        webMQProperties.setErrorQueueManager("TestManager");
        Mockito.when(webMqConfiguration.consumerConnectionFactory()).thenThrow(new RuntimeException());
        ConnectionFactory connectionFactory= webMqConfiguration.producerConnectionFactory();
        Assert.assertNotNull(connectionFactory);
    }

    @Test
    public void testJMSConsumerConfiguration(){
        ConnectionFactory consumerConnectionFactory= getConnectionFactory();
        PIDCredentials pidCredentials = new PIDCredentials("ttest","test");
        JmsConfiguration jmsConfiguration = webMqConfiguration.jmsConsumerConfiguration(consumerConnectionFactory,pidCredentials);
        Assert.assertNotNull(jmsConfiguration);

    }

    @Test
    public void testJMSProducerConfiguration(){
        ConnectionFactory consumerConnectionFactory=getConnectionFactory();
        PIDCredentials pidCredentials = new PIDCredentials("ttest","test");
        JmsConfiguration jmsConfiguration = webMqConfiguration.jmsProducerConfiguration(consumerConnectionFactory,pidCredentials);
        Assert.assertNotNull(jmsConfiguration);

    }

    @Test
    public void testWebSphereConsumer(){
        JmsConfiguration jmsConsumerConfiguration= new JmsConfiguration();
        JmsComponent jmsComponent =webMqConfiguration.webSphereConsumer(jmsConsumerConfiguration);
        Assert.assertNotNull(jmsComponent);
    }

    @Test
    public void testWebSphereProducer(){
        JmsConfiguration jmsProducerConfiguration= new JmsConfiguration();
        JmsComponent jmsComponent =webMqConfiguration.webSphereProducer(jmsProducerConfiguration);
        Assert.assertNotNull(jmsComponent);

    }

    private ConnectionFactory getConnectionFactory(){
      return   new ConnectionFactory() {
            @Override
            public Connection createConnection() throws JMSException {
                return null;
            }

            @Override
            public Connection createConnection(String userName, String password) throws JMSException {
                return null;
            }

           @Override
            public JMSContext createContext() {
                return null;
            }

            @Override
            public JMSContext createContext(int sessionMode) {
                return null;
            }

            @Override
            public JMSContext createContext(String userName, String password) {
                return null;
            }

            @Override
            public JMSContext createContext(String userName, String password, int sessionMode) {
                return null;
            }
        };
    }
}
