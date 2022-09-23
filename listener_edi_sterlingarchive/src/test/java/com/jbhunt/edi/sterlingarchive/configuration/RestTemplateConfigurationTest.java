package com.jbhunt.edi.sterlingarchive.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class RestTemplateConfigurationTest {

    @Test
    public void testRestTemplateConfiguration(){
        RestTemplateConfiguration restTemplateConfiguration = new RestTemplateConfiguration(new RestTemplateBuilder());
        assertNotNull(restTemplateConfiguration.restTemplate());
    }
}
