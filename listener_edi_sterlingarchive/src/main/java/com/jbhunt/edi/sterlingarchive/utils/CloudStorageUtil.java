package com.jbhunt.edi.sterlingarchive.utils;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.gson.Gson;
import com.jbhunt.edi.sterlingarchive.constants.SterlingArchiveConstants;
import com.jbhunt.edi.sterlingarchive.storage.GoogleCloudStorage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.jbhunt.edi.sterlingarchive.constants.SterlingArchiveConstants.FOLDER_STRUCTURE;

@Slf4j
@Component
@Data
public class CloudStorageUtil {

    @Value("${GCP.bucket}")
    private String bucketName;

    private GoogleCloudStorage storage;
    private static final String RATE_RESPONSE_FOLDER = "EDIDocuments";

    public CloudStorageUtil (GoogleCloudStorage storage){
        this.storage = storage;
    }

    /**
     * Uploads LTLRateFilePackage to GCP Bucket as JSON File
     * @param file
     */
    public void uploadEDIDoc(String file, String sciObjectID, String millis, String uid)  {
        LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String folderStructure = folderStructureDocuments(localDate);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("guid",uid);
        BlobId blobId = BlobId.of(bucketName, FOLDER_STRUCTURE + "/" + uid);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setMetadata(metadata).setContentType("application/json").build();
        storage.getStorage().create(blobInfo, new Gson().toJson(file).getBytes());
    }

    private String folderStructureDocuments(LocalDate localDate) {
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        return SterlingArchiveConstants.DOCUMENTS.replace(SterlingArchiveConstants.YEAR, String.valueOf(year))
                .replace(SterlingArchiveConstants.MONTH, String.valueOf(month))
                .replace(SterlingArchiveConstants.DAY, String.valueOf(day));
    }
}
