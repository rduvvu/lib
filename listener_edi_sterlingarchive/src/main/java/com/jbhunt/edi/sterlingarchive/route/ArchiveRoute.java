package com.jbhunt.edi.sterlingarchive.route;

import com.jbhunt.edi.sterlingarchive.constants.SterlingArchiveConstants;
import com.jbhunt.edi.sterlingarchive.exception.DocumentProcessingException;
import com.jbhunt.edi.sterlingarchive.processor.DocumentProcessor;
import com.jbhunt.edi.sterlingarchive.processor.OnErrorProcessor;
import com.jbhunt.edi.sterlingarchive.processor.PostProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipException;

@Component
@RefreshScope
public class ArchiveRoute extends RouteBuilder {

    @Value("${messaging.webMq.archive.queueName}")
    private String webMqName;

    @Value("${messaging.webMq.error.queueName}")
    private String errorQueue;

    @Value("${spring.profiles.active}")
    private String profile;

    private final DocumentProcessor documentProcessor;
    private final OnErrorProcessor onErrorProcessor;
    private final PostProcessor postProcessor;

    public ArchiveRoute(DocumentProcessor documentProcessor, OnErrorProcessor onErrorProcessor,
                        PostProcessor postProcessor) {
        this.documentProcessor = documentProcessor;
        this.onErrorProcessor = onErrorProcessor;
        this.postProcessor = postProcessor;
    }

    @Override
    public void configure() {
        String errorQueueName;
        String consumerQueueName;
        if(profile.equalsIgnoreCase(SterlingArchiveConstants.SPRING_LOCAL_PROFILE)) {
            consumerQueueName = SterlingArchiveConstants.CAMEL_ROUTE_AMQ_CONSUMER.concat(webMqName);
            errorQueueName = SterlingArchiveConstants.CAMEL_ROUTE_AMQ_PRODUCER.concat(errorQueue);
        } else {
            consumerQueueName =SterlingArchiveConstants.CAMEL_ROUTE_WEBMQ_CONSUMER.concat(webMqName);
            errorQueueName = SterlingArchiveConstants.CAMEL_ROUTE_WEB_MQ_PRODUCER.concat(errorQueue);
        }
        this.getContext().setAllowUseOriginalMessage(true);


        //after the document is not XML or invalid XML
        onException(DocumentProcessingException.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, SterlingArchiveConstants.CAMEL_ROUTE_LOG_DOCUMENT_PROCESSING_EXCEPTION)
                .process(onErrorProcessor)
                .to(errorQueueName)
                .end();

        //after the document is not XML, invalid XML, or not ProcessData
        onException(IOException.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, SterlingArchiveConstants.CAMEL_ROUTE_LOG_IO_EXCEPTION)
                .process(onErrorProcessor)
                .to(errorQueueName)
                .end();


        //after any other exception
        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, SterlingArchiveConstants.CAMEL_ROUTE_LOG_EXCEPTION)
                .process(onErrorProcessor)
                .to(errorQueueName)
                .end();

        onException(ZipException.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, SterlingArchiveConstants.CAMEL_ROUTE_LOG_ZIP_EXCEPTION)
                .process(onErrorProcessor)
                .end();

        // Enable Logging to the default logger
        errorHandler(defaultErrorHandler().log(log));

        //Send documents from queue this listener reads from
        //(WebMQ) directly to the main process SEDA to allow for
        //async document processing
        from(consumerQueueName)
                .log(SterlingArchiveConstants.CAMEL_ROUTE_LOG_BODY)
                .to(SterlingArchiveConstants.CAMEL_ROUTE_MAIN_PROCESS_SEDA)
                .end();

        //from SEDA, through processor
        from(SterlingArchiveConstants.CAMEL_ROUTE_MAIN_PROCESS_SEDA)
                .threads(20)
                .process(documentProcessor)
                .to(SterlingArchiveConstants.CAMEL_ROUTE_POST_PROCESSOR_DIRECT)
                .end();

        //postprocessor checks exchange to see if exception property
        //was set on it (since the async doc processor cannot throw
        //exception)
        from(SterlingArchiveConstants.CAMEL_ROUTE_POST_PROCESSOR_DIRECT)
                .process(postProcessor)
                .end();
    }
}
