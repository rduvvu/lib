package com.jbhunt.edi.sterlingarchive.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@RefreshScope
@ConfigurationProperties("messaging")
public class WebMQProperties {
    private String archiveHost;
    private int archivePort;
    private String archiveQueueManager;
    private String archiveChannel;
    private String errorHost;
    private int errorPort;
    private String errorQueueManager;
    private String errorChannel;
}