package com.jbhunt.edi.sterlingarchive.utils;

import com.google.cloud.storage.Storage;
import com.jbhunt.edi.sterlingarchive.repository.DataRepository;
import com.jbhunt.edi.sterlingarchive.storage.GoogleCloudStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class CloudStorageUtilTest {

    @InjectMocks
    private CloudStorageUtil cloudStorageUtil;

    @Mock
    private GoogleCloudStorage storage;

    @Mock
    private Storage mockstorage;

    @Test
    public void uploadEDIDocTest() throws NullPointerException {
        cloudStorageUtil.setBucketName("TEST");
        cloudStorageUtil.setStorage(storage);
        when(storage.getStorage()).thenReturn(mockstorage);
        cloudStorageUtil.uploadEDIDoc("TESTFile", "SCIOBJID", "12345", "123");
    }

}
