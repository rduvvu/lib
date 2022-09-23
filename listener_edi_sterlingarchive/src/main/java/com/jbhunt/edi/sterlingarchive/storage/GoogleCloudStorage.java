package com.jbhunt.edi.sterlingarchive.storage;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleCloudStorage {

    @Value("${GCP.project}")
    String projectId;

    @Getter
    private final Storage storage;

    public GoogleCloudStorage() {
        storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    }

}
