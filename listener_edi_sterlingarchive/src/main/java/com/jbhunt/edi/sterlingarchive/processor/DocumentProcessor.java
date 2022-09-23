package com.jbhunt.edi.sterlingarchive.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbhunt.edi.sterlingarchive.dto.DocumentReferenceDTO;
import com.jbhunt.edi.sterlingarchive.dto.MetaDataDTO;
import com.jbhunt.edi.sterlingarchive.dto.NineNineSevenResponseDTO;
import com.jbhunt.edi.sterlingarchive.exception.DocumentProcessingException;
import com.jbhunt.edi.sterlingarchive.repository.DataRepository;
import com.jbhunt.edi.sterlingarchive.utils.CloudStorageUtil;
import com.jbhunt.edi.sterlingarchive.utils.ExtractorUtil;
import com.jbhunt.requestmetric.annotation.RequestMetric;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import static com.jbhunt.edi.sterlingarchive.constants.SterlingArchiveConstants.*;


@Component
@Slf4j
@RefreshScope
public class DocumentProcessor implements AsyncProcessor {

    private final DataRepository dataRepository;
    private final CloudStorageUtil cloudStorageUtil;

    public DocumentProcessor(DataRepository dataRepository, CloudStorageUtil cloudStorageUtil) {

        this.dataRepository = dataRepository;
        this.cloudStorageUtil = cloudStorageUtil;
    }

    @RequestMetric
    public boolean process(Exchange exchange, AsyncCallback callback) {
        String sciObjectID =null;
        String workFlowId = null;
        String doctype = null;
        String processDataXmlStr = null;
        Instant startProcess = Instant.now();

        try {
            //System.out.println("message");
            MetaDataDTO metaDataDTO =  MetaDataDTO.builder().build();
            processDataXmlStr = exchange.getIn().getBody(String.class);
            String millis = String.valueOf(Instant.now().toEpochMilli());

            if (StringUtils.isNotEmpty(processDataXmlStr)) {
                ExtractorUtil extractorUtil = new ExtractorUtil(processDataXmlStr);
                Optional<String>  sciObjectIDWithColons = extractorUtil.extractSCIObjectID();
                if(sciObjectIDWithColons.isPresent()) {
                    sciObjectID = sciObjectIDWithColons.get().toLowerCase().replace(":", "-");
                }else{
                    sciObjectID = NOT_FOUND;
                }
                Optional<String>  workflowId = extractorUtil.dualExtract();
                if(workflowId.isPresent()) {
                    workFlowId = workflowId.get();
                }else{
                    workFlowId = NOT_FOUND;
                }
                Optional<String>  docType = extractorUtil.extractDocType();
                if(docType.isPresent()) {
                    doctype = docType.get();
                }else{
                    doctype = NOT_FOUND;
                }
                log.info("**************** sciObjectId : {} , workFlowId : {} , doctype : {} : Start Processing *****************",sciObjectID, workFlowId, doctype);
                if(sciObjectID.equalsIgnoreCase(NOT_FOUND)){

                    log.error("{}: >>> DocumentParsingError: Cannot Find SCIObjectID <<<" ,sciObjectID);
                    throw new DocumentProcessingException("DocumentProcessingError:  Cannot Find SCIObjectID");
                }else{
                    metaDataDTO.setDocId(sciObjectID);
                }

                extractorUtil.extractMultipleField(metaDataDTO);
                log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : Extracted Fields & Start Uploading Documents" , sciObjectID, workFlowId, doctype);

                SimpleDateFormat cstCdtFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                cstCdtFormat.setTimeZone(TimeZone.getTimeZone("CST6CDT"));

                metaDataDTO.setInsertedInstant(cstCdtFormat.format(new Date()));
                checkForNeededValuesInInputMessage(processDataXmlStr, extractorUtil, metaDataDTO);
               // First Upload ProcessDocument or Actual Message From Queue
                uploadProcessDocument(processDataXmlStr, sciObjectID, workFlowId, doctype, metaDataDTO,millis);

                // Check for PreData and PostData Start Uploading the EdiDoc and RawDoc
                uploadEdiRawAndAdfDocument(processDataXmlStr, extractorUtil, sciObjectID, workFlowId, doctype, metaDataDTO,millis);

                // Check for Error in the Document if found then create an TranslationReport Error report & Upload to Document in blob Storage
                uploadTranslationErrorReportDocument(processDataXmlStr,extractorUtil, sciObjectID, workFlowId, doctype, metaDataDTO,millis);

                Duration completedDuration = Duration.between(startProcess,Instant.now());
                log.info(" **************** sciObjectId : {} , workFlowId : {} , doctype : {} : , TotalMilliSeconds: {} ,TotalSeconds : {} Completed ****************",
                        sciObjectID, workFlowId, doctype,completedDuration.toMillis(),Math.abs(completedDuration.getSeconds())) ;

            }
            else{
                log.error(" >>> DocumentProcessingError:  Message Body Cannot be Empty <<<");
                throw new DocumentProcessingException("DocumentProcessingError:  Message Body Cannot be Empty");
            }

        }catch(Exception e){
            log.error("sciObjectId : {} , workFlowId : {} , doctype : {} : >>> ApplicationException: {} <<<", sciObjectID, workFlowId, doctype, e);
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);
        }
        callback.done(false);
        return false;

    }

    @Override
    public CompletableFuture<Exchange> processAsync(Exchange exchange) {
        return null;
    }

    public void process(Exchange exchange) {
        log.error("DocumentProcessingError: This process is meant to be async, so this message should not be seen : ExchangeId: {} , FromRouteId: {}"+exchange.getExchangeId(),exchange.getFromRouteId());
    }

    public void checkForNeededValuesInInputMessage(String processDataXmlStr, ExtractorUtil extractorUtil, MetaDataDTO metaDataDTO) throws XPathExpressionException {
        if(isError(processDataXmlStr, extractorUtil)){
            if(metaDataDTO.getSender()== null || metaDataDTO.getSender().isEmpty()){
                metaDataDTO.setSender(UNKNOWN);
            }
            if(metaDataDTO.getReceiver()== null || metaDataDTO.getReceiver().isEmpty()){
                metaDataDTO.setReceiver((UNKNOWN));
            }
            if(metaDataDTO.getTradingPartner()== null || metaDataDTO.getTradingPartner().isEmpty()){
                metaDataDTO.setTradingPartner(UNKNOWN);
            }
            if(metaDataDTO.getDocType()== null || metaDataDTO.getDocType().isEmpty()){
                metaDataDTO.setDocType(UNKNOWN);
            }
            if(metaDataDTO.getDirection()== null || metaDataDTO.getDirection().isEmpty()){
                metaDataDTO.setDirection(INBOUND);
            }
            metaDataDTO.setProcessStatus(ERROR_PRS_STT);
        }
    }

    public void uploadProcessDocument(String processDataXmlStr,String sciObjectID, String workFlowId, String doctype,
                                      MetaDataDTO metaDataDTO,String millis) throws IOException, ParseException {
        String processDataDocBlobName = sciObjectID.concat("-").concat(millis).concat(PROCESS_DOC_SUFFIX);
        log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : uploading processData with UID {} to {} ", sciObjectID, workFlowId, doctype, processDataDocBlobName, FOLDER_STRUCTURE);
        Instant start = Instant.now();
        cloudStorageUtil.uploadEDIDoc(processDataXmlStr, sciObjectID, millis, processDataDocBlobName);
        Duration duration = Duration.between(start,Instant.now());
        metaDataDTO.setProcessDataDocRef(new DocumentReferenceDTO(processDataDocBlobName, FOLDER_STRUCTURE));
        metaDataDTO.setDocTypeToInsert("PROCESS");
        uploadToEDIPRD(sciObjectID, workFlowId, doctype,metaDataDTO, processDataDocBlobName);
        log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : TotalMilliSeconds: {} , TotalSeconds: {} : uploaded processData {}",
                sciObjectID, workFlowId, doctype,duration.toMillis(),Math.abs(duration.getSeconds()),processDataDocBlobName);
    }

    public void uploadTranslationErrorReportDocument(String processDataXmlStr,ExtractorUtil extractorUtil,String sciObjectID,
                                                        String workFlowId, String doctype, MetaDataDTO metaDataDTO,String millis) throws XPathExpressionException, IOException, ParseException {
        Instant start = Instant.now();
        if (isError(processDataXmlStr, extractorUtil)) {
            metaDataDTO.setError(true);
            String errorMessage = "";
            Optional<String> advStatus = extractorUtil.getNode("/ProcessData/ERRORHANDLING_ProcessData/ERROR_SERVICE/ADV_STATUS");
            if(advStatus.isPresent()){
                errorMessage = advStatus.get();
            }
            Optional<String> svcName = extractorUtil.getNode("/ProcessData/ERRORHANDLING_ProcessData/ERROR_SERVICE/SERVICE_NAME");
            if(svcName.isPresent()){
                errorMessage = errorMessage.concat(svcName.get());
            }
            if (!errorMessage.isEmpty() && errorMessage.length()>1) {
                metaDataDTO.setErrorMessage(errorMessage);
            }
            Optional<String> errorCRT_S = extractorUtil.getNode("/ProcessData/ERRORHANDLING_ProcessData/CurDate");
            if(errorCRT_S.isPresent()){
                metaDataDTO.setErrorCRT_S(errorCRT_S.get());
            }
            String translationErrorDocBlobName = sciObjectID.concat("-").concat(millis).concat(TRANSLATION_DOC_SUFFIX);
        Optional<String> translationReport = extractorUtil.extractTranslationReport();
        metaDataDTO.setDocTypeToInsert(ERROR_PRS_STT);
        if (translationReport.isPresent()) {
            log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : uploading translationErrorDocReport with UID {} to {} ", sciObjectID, workFlowId, doctype, translationErrorDocBlobName, FOLDER_STRUCTURE);
            cloudStorageUtil.uploadEDIDoc(translationReport.get(), sciObjectID, millis, translationErrorDocBlobName);
            Duration duration = Duration.between(start,Instant.now());
            log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : TotalMilliSeconds: {} , TotalSeconds: {} : uploaded translationErrorDocReport {}", sciObjectID, workFlowId, doctype,
                    duration.toMillis(),Math.abs(duration.getSeconds()),translationErrorDocBlobName);
        }
            metaDataDTO.setTranslationErrorDocRef(new DocumentReferenceDTO(translationErrorDocBlobName, FOLDER_STRUCTURE));
            uploadToEDIPRD(sciObjectID, workFlowId, doctype,metaDataDTO, translationErrorDocBlobName);
            dataRepository.insertToNewErrMsgLogTable(metaDataDTO,translationErrorDocBlobName );
            log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : uploaded translationErrorDocReport to Error Msg Log table with UID {} ", sciObjectID, workFlowId, doctype, translationErrorDocBlobName);
        }
    }

    public boolean isError(String processDataXmlStr, ExtractorUtil extractorUtil) throws XPathExpressionException {
        Boolean errorFlag = processDataXmlStr.contains("<ERRORHANDLING_ProcessData>");
        if(!errorFlag){
            errorFlag = extractorUtil.getNodes("/ProcessData/ERRORHANDLING_ProcessData").size() >0;
        }
        return errorFlag;
    }

    public void uploadEdiRawAndAdfDocument(String processDataXmlStr, ExtractorUtil extractorUtil,String sciObjectID,String workFlowId,
                                           String doctype,MetaDataDTO metaDataDTO,String millis) throws XPathExpressionException, IOException, ParseException {
        Instant start = Instant.now();
        String ediTagName =null;
        String adfTagName = null;
        String ediRawDocBlobName = sciObjectID.concat("-").concat(millis).concat(EDI_DOC_SUFFIX);
        String adfDocBlobName = sciObjectID.concat("-").concat(millis).concat(ADF_DOC_SUFFIX);
        String docTypeToInsert = null;
        ediTagName = "PreData";
        adfTagName = "PostData";
        Optional<String> origEdiRawData = extractorUtil.getNode("/ProcessData/" + ediTagName);
        if (origEdiRawData.isPresent()) {
            Optional<String> unescapedEdiData = Optional.ofNullable(StringEscapeUtils.unescapeHtml4(origEdiRawData.get()));
            if (unescapedEdiData.isPresent()) {
                log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : uploading origEdiRawData with UID {} to {} ", sciObjectID, workFlowId, doctype, ediRawDocBlobName, FOLDER_STRUCTURE);
                cloudStorageUtil.uploadEDIDoc(unescapedEdiData.get(), sciObjectID, millis, ediRawDocBlobName);
                metaDataDTO.setEdiRawDocRef(new DocumentReferenceDTO(ediRawDocBlobName, FOLDER_STRUCTURE));
                Duration duration = Duration.between(start,Instant.now());
                metaDataDTO.setDocTypeToInsert(getDocTypeValueToInsert(metaDataDTO, ediTagName));
                log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : TotalMilliSeconds: {} , TotalSeconds: {} : uploaded origEdiRawData {}", sciObjectID, workFlowId, doctype,
                        duration.toMillis(),Math.abs(duration.getSeconds()),ediRawDocBlobName);
                uploadToEDIPRD(sciObjectID, workFlowId, doctype,metaDataDTO, ediRawDocBlobName);

                if(checkifNineNineSeven(extractorUtil)){
                    Optional<String> ackDocType = extractorUtil.getNode("/ProcessData/FunctionalAcknowledgment/DetailInformation/AckDocumentType");
                    if(ackDocType.isPresent()){
                        metaDataDTO.setDocTypeToInsert(ackDocType.get());
                    }
                    Optional<String> gsGrpCtrlNbr = extractorUtil.getNode("/ProcessData/FunctionalAcknowledgment/DetailInformation/AckDocumentGSNumber");
                    if(gsGrpCtrlNbr.isPresent() ){
                        metaDataDTO.setGsGrpCtrlNbrToInsert(gsGrpCtrlNbr.get());
                    }
                    List<NineNineSevenResponseDTO> nineNineSevenDataList = dataRepository.selectNineNineSevenMetaData(metaDataDTO);
                    for (NineNineSevenResponseDTO nineNineSevenData : nineNineSevenDataList) {
                        if (nineNineSevenData.getEdiDocHdrI() != null && ediRawDocBlobName != null) {
                            dataRepository.insertToNewEDIIDXTable(metaDataDTO, nineNineSevenData.getEdiDocHdrI(), NINE_NINE_SEVEN_KEY, ediRawDocBlobName);
                            dataRepository.insertToCompleteOrigDoc(nineNineSevenData.getEdiDocHdrI());
                        }
                    }
                }
                else{
                    String ediDocHdrI = dataRepository.selectEdiDocHdrI(metaDataDTO);
                    metaDataDTO.setEdiDocHdrI(ediDocHdrI);
                    uploadToIDXTable(sciObjectID, workFlowId, doctype, metaDataDTO, ediDocHdrI);
                }

                log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : TotalMilliSeconds: {} , TotalSeconds: {} : uploaded origEdiRawData to IDX table{}", sciObjectID, workFlowId, doctype,
                        duration.toMillis(),Math.abs(duration.getSeconds()),ediRawDocBlobName);
            }
        } else {
            log.error("sciObjectId : {} , workFlowId : {} , doctype : {} : >>> Cannot upload EdiRawData : Document does not contain  : {} <<<", sciObjectID, workFlowId, doctype, ediTagName);
        }

        Optional<String> adfData = extractorUtil.getNode("/ProcessData/" + adfTagName);
        if (adfData.isPresent()) {
            Optional<String> unescapedRawData = Optional.ofNullable(StringEscapeUtils.unescapeHtml4(adfData.get()));
            if (unescapedRawData.isPresent()) {
                log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : uploading adfData with UID {} to {} ", sciObjectID, workFlowId, doctype, adfDocBlobName, FOLDER_STRUCTURE);
                cloudStorageUtil.uploadEDIDoc(unescapedRawData.get(), sciObjectID, millis, adfDocBlobName);
                metaDataDTO.setAdfDocRef(new DocumentReferenceDTO(adfDocBlobName, FOLDER_STRUCTURE));
                Duration duration = Duration.between(start,Instant.now());
                metaDataDTO.setDocTypeToInsert(getDocTypeValueToInsert(metaDataDTO, adfTagName));
                log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : TotalMilliSeconds: {} , TotalSeconds: {} : uploaded adfData {}", sciObjectID, workFlowId, doctype,
                        duration.toMillis(),Math.abs(duration.getSeconds()),adfDocBlobName);
                uploadToEDIPRD(sciObjectID, workFlowId, doctype,metaDataDTO, adfDocBlobName);
            }
        } else {
            log.error("sciObjectId : {} , workFlowId : {} ,doctype : {} : >>> Cannot upload adfData : Document does not contain  : {} <<<", sciObjectID, workFlowId, doctype, adfTagName );
        }

    }

    public Boolean checkifNineNineSeven(ExtractorUtil extractorUtil) throws XPathExpressionException {
        Optional<String> trSetIdCode = extractorUtil.getNode("/ProcessData/TransactionSetIDCode");
        if(trSetIdCode.isPresent() && trSetIdCode.get().equals("997")){
            return true;
        }
        else
            return false;
    }

    private void uploadToEDIPRD(String sciObjectID, String workFlowId, String doctype, MetaDataDTO metaDataDTO,
                                   String refName) throws IOException, ParseException {
        log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : Messaging Metadata to EDIPRD {} ", sciObjectID, workFlowId, doctype, FOLDER_STRUCTURE);
        Instant start = Instant.now();
        String finalMetadataStr = new ObjectMapper().writeValueAsString(metaDataDTO);
        //INBOUND will be docType and RAW for pre and post data
        //OUTBOUND will be RAW and docType for pre and post data
        dataRepository.insertToNewEDITable(metaDataDTO, refName);
        Duration duration = Duration.between(start,Instant.now());
        log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : TotalMilliSeconds: {} , TotalSeconds: {}: Messaged Metadata to EDIPRD HDR Table {} :",
                sciObjectID, workFlowId, doctype,duration.toMillis(),Math.abs(duration.getSeconds()),finalMetadataStr) ;
    }

    private void uploadToIDXTable(String sciObjectID, String workFlowId, String doctype, MetaDataDTO metaDataDTO,
                                  String ediDocHDRI) throws  IOException, ParseException {
        Instant start = Instant.now();
        String finalMetadataStr = new ObjectMapper().writeValueAsString(metaDataDTO);
        //corelationID needs to show up as EdiKey1
        //DocKey1 values show up as multiple KeyVal1 entries
        for(String ediKey: metaDataDTO.getEdiKeys()){
            dataRepository.insertToNewEDIIDXTable(metaDataDTO,ediDocHDRI, EDIKEY1, ediKey );
        }
        for(String docKey: metaDataDTO.getDocKeys()){
            dataRepository.insertToNewEDIIDXTable(metaDataDTO, ediDocHDRI, KEYVAL1, docKey);
        }
        if(metaDataDTO.getWmqCorrelationId()!= null && !metaDataDTO.getWmqCorrelationId().isEmpty()){
            dataRepository.insertToNewEDIIDXTable(metaDataDTO, ediDocHDRI, TRACKING_ID, metaDataDTO.getWmqCorrelationId());
        }

        Duration duration = Duration.between(start,Instant.now());
        log.info("sciObjectId : {} , workFlowId : {} , doctype : {} : TotalMilliSeconds: {} , TotalSeconds: {}: Messaged Metadata to EDIPRD IDX Table {} :",
                sciObjectID, workFlowId, doctype,duration.toMillis(),Math.abs(duration.getSeconds()),finalMetadataStr) ;
    }

    public String getDocTypeValueToInsert(MetaDataDTO metaDataDTO, String tagName) {
        //INBOUND will be docType and RAW for pre and post data
        //OUTBOUND will be RAW and docType for pre and post data
        if (tagName.equals("PreData")) {
            return metaDataDTO.getDocType();
        } else if (tagName.equals("PostData")) {
            return "RAW";
        }
        return "";
    }
}
