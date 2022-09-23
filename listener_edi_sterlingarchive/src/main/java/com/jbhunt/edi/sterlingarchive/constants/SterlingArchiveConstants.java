package com.jbhunt.edi.sterlingarchive.constants;

public class SterlingArchiveConstants {

    public static final String CAMEL_ROUTE_ERROR_DIRECT = "direct:error";
    public static final String CAMEL_ROUTE_INCREMENT_DIRECT = "direct:incrementretry";
    public static final String CAMEL_ROUTE_POST_PROCESSOR_DIRECT = "direct:postprocessor";
    public static final String CAMEL_ROUTE_MAIN_PROCESS_SEDA ="seda:mainProcess";
    public static final String CAMEL_ROUTE_AMQ_CONSUMER ="activeAmqConsumer:";
    public static final String CAMEL_ROUTE_AMQ_PRODUCER ="activeMQProducer:";
    public static final String CAMEL_ROUTE_WEBMQ_CONSUMER ="webSphereConsumer:queue:";
    public static final String CAMEL_ROUTE_WEB_MQ_PRODUCER ="webSphereProducer:queue:";

    public static final String CAMEL_ROUTE_LOG_ZIP_EXCEPTION ="ZipException caught";
    public static final String CAMEL_ROUTE_LOG_DOCUMENT_PROCESSING_EXCEPTION ="DocumentProcessing Exception caught.";
    public static final String CAMEL_ROUTE_LOG_EVENT_HUB_EXCEPTION ="EventHub Exception caught.";
    public static final String CAMEL_ROUTE_LOG_BLOB_EXCEPTION ="Blob Storage Exception caught.";
    public static final String COSMOS_LOG_BLOB_EXCEPTION ="Blob Storage Exception caught.";

    public static final String CAMEL_ROUTE_LOG_IO_EXCEPTION ="IOException caught.";
    public static final String CAMEL_ROUTE_LOG_EXCEPTION ="Exception caught.";
    public static final String CAMEL_ROUTE_LOG_BODY ="${body}";

    public static final String SPRING_LOCAL_PROFILE="LOCAL";
    public static final String SPRING_NOT_LOCAL_PROFILE="!LOCAL";

    public static final String NOT_FOUND ="NOT_FOUND";
    public static final String SUFFIX_PARSE_ERROR ="parse-error";

    public static final String DOCUMENTS = "documents/{yyyy}/{mm}/{dd}";
    public static final String METADATA = "metadata/{yyyy}/{mm}/{dd}";

    public static final String EDI_DOC_SUFFIX = "-edi";
    public static final String ADF_DOC_SUFFIX  = "-adf";
    public static final String PROCESS_DOC_SUFFIX = "-process";
    public static final String METADATA_DOC_SUFFIX  = "-metadata";
    public static final String TRANSLATION_DOC_SUFFIX  = "-error";

    public static final String YEAR  = "{yyyy}";
    public static final String MONTH  = "{mm}";
    public static final String DAY  = "{dd}";

    public static final String COSMOS_OK_STATUS ="ok";
    public static final String INBOUND ="INBOUND";
    public static final String OUTBOUND ="OUTBOUND";
    public static final String SERVER_STATUS ="serverStatus";
    public static final String FOLDER_STRUCTURE ="EDIDocuments";
    public static  final String EDIKEY1 ="EdiKey1";
    public static  final String KEYVAL1 ="KeyVal1";
    public static final String NINE_NINE_SEVEN_KEY ="997Key";
    public static final String TRACKING_ID = "trackingID";
    public static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
    public static final String CRT_PGM_C = "Java archive listener";
    public static final String ERROR_PRS_STT = "ERROR";
    public static final String RTRY_CNT_Q = "1";
    public static final String REC_STT = "A";
    public static final String UNKNOWN = "unknown";



}
