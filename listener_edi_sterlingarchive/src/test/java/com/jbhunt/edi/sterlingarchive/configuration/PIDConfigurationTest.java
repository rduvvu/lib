package com.jbhunt.edi.sterlingarchive.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
public class PIDConfigurationTest {

    @InjectMocks
    PIDConfiguration pidConfiguration;

    @Test
    public void testGetpidCredentials(){
        ReflectionTestUtils.setField(pidConfiguration, "userID", "test");
        ReflectionTestUtils.setField(pidConfiguration, "password", "test");
        com.jbhunt.biz.securepid.PIDCredentials pidCreds= pidConfiguration.pidCredentials();
        assertEquals("test",pidCreds.getPassword());
        assertEquals("test",pidCreds.getUsername());
        pidConfiguration.setPassword("Test1");
        pidConfiguration.setUserID("Test1");
        assertEquals("Test1",pidConfiguration.getPassword());
        assertEquals("Test1", pidConfiguration.getUserID());
        pidConfiguration.hashCode();
        assertFalse(pidConfiguration.equals(pidCreds));
          }
}
