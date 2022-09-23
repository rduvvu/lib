package com.jbhunt.edi.sterlingarchive.utils;

import com.jbhunt.edi.sterlingarchive.dto.MetaDataDTO;
import com.jbhunt.edi.sterlingarchive.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ExtractorUtil {

    private String str;
    private Document document;
    private XPath xPath;
    private String sciObjectId;


    ExtractorUtil() {
        xPath = XPathFactory.newInstance().newXPath();
    }

    public ExtractorUtil(String str)  {
        this();
        this.str = str;
        this.document = convertStringToDocument(this.str);
    }


    public Optional<String> dualExtract() throws XPathExpressionException {
        Optional<String> processId = getNode("/ProcessData/PROCESS_ID");
        if (processId.isPresent()) {
            return processId;
        }
        Optional<String> myWorkflowID = getNode("/ProcessData/myWorkflowID");
        if (!myWorkflowID.isPresent())
            myWorkflowID = getNode("/ProcessData/BPDATA/WORKFLOW_ID");
        if (!myWorkflowID.isPresent())
            myWorkflowID = getNode("/ProcessData/ERRORHANDLING_ProcessData/ErrorBPData/BPDATA/WORKFLOW_ID");
        if (!myWorkflowID.isPresent())
            myWorkflowID = getNode("/ProcessData/ERRORHANDLING_ProcessData/BPDATA/WORKFLOW_ID");
        if (!myWorkflowID.isPresent())
            myWorkflowID = getNode("/ProcessData/ERRORHANDLING_ProcessData/DocumentWorkflowId");
        if (myWorkflowID.isPresent()) {
            return myWorkflowID;
        }
        return Optional.empty();
    }

    public Optional<String> getNode(String xpathExpression) throws XPathExpressionException {
        if (StringUtils.isNotEmpty(xpathExpression)) {
            XPathExpression expr = xPath.compile(xpathExpression);
            // Create XPathExpression object
            Object objNode = expr.evaluate(document, XPathConstants.NODE);
            if (objNode != null) {
                Node node = (Node) objNode;
                if(node instanceof CharacterData){
                    CharacterData characterData = ((CharacterData)node);
                    int docLength =characterData.getLength();
                    if(docLength > 0){
                        return Optional.ofNullable(characterData.getData());
                    }
                }else{
                    return Optional.ofNullable(node.getTextContent());
                }
            }
        }
        return Optional.empty();
    }

    public List<String> getNodes(String xpathExpression) throws XPathExpressionException {
        XPathExpression expr = xPath.compile(xpathExpression);
        NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        List<String> values = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = (Node) nodes.item(i);
            if(node instanceof CharacterData){
                values.add(((CharacterData)node).getData());
            }else{
                values.add(node.getTextContent());
            }
        }
        return values;
    }


    public Optional<String> extractSCIObjectID() throws XPathExpressionException {

        Optional<String> result = getNode("/ProcessData/PrimaryDocument/@SCIObjectID");
        if (!result.isPresent())
            result = getNode("/ProcessData/DocSave/@SCIObjectID");
        if (!result.isPresent())
            result = getNode("/ProcessData/OriginalDocument/@SCIObjectID");
        if (!result.isPresent())
            result = getNode("/ProcessData/PreDataFile/@SCIObjectID");

        if (result.isPresent()) {
            sciObjectId = result.get();
        }
        return result;
    }

    public Optional<String> extractDocType() throws XPathExpressionException {
        Optional<String> docAltTypeField2 =getDocTypeAlternate();
        Optional<String> docTypeField = getDocType();
        if (docTypeField.isPresent()) {
            return docTypeField;
        } else if (docAltTypeField2.isPresent()) {
           return   docAltTypeField2;
        }
        return Optional.empty();
    }

    private Document convertStringToDocument(String xmlStr)  {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            return doc;
        }catch (Exception e){
            throw new DocumentProcessingException(e);
        }
    }
    private void extractEdiKeys(MetaDataDTO metaDataDTO) throws XPathExpressionException {
        // Remove Duplicates
        Set<String> set = new LinkedHashSet<>();
        List<String> correlationIds = getNodes("/ProcessData/MapResult/Row[*]/Correlation_ID");
        if (correlationIds.size() > 0) {
            // Remove duplicates
            set.addAll(correlationIds);
            correlationIds.clear();
        }
        List<String> correlationId = getNodes("/ProcessData/Correlation_ID");
        if (correlationId.size() > 0) {
            set.addAll(correlationId);
        }
        // Extract Error Conditions

        List<String> errorCorrelationId = getNodes("/ProcessData/ERRORHANDLING_ProcessData/MapResult/Row[*]/Correlation_ID");
        if (errorCorrelationId.size() > 0) {
            set.addAll(errorCorrelationId);
        }

        metaDataDTO.setEdiKeys(set);
    }

    public Optional<String> extractTranslationReport() throws XPathExpressionException {
        Optional<String> result = getNode("/ProcessData/ERRORHANDLING_ProcessData/stat_rpt");
        if (!result.isPresent()) {
            result = getNode("/ProcessData/stat_rpt");
        }
        return result;
    }

    public Optional<String> getDocTypeAlternate() throws XPathExpressionException {
        Optional<String> docTypeAltField = getNode("/ProcessData/TransactionSetIDCode");// Need To check for XML format
        Optional<String> docTypeAltField1 = getNode("/ProcessData/TransationInfo/TransactionSetIDCode");
        Optional<String> docAltTypeField2 = docTypeAltField.isPresent() ? docTypeAltField : docTypeAltField1;
        return docAltTypeField2;
    }

    public Optional<String> getDocType() throws XPathExpressionException {
        Optional<String> docTypeField = getNode("/ProcessData/MapResult/Row/DOC_TYPE");
        if (!docTypeField.isPresent())
            docTypeField = getNode("/ProcessData/ERRORHANDLING_ProcessData/DOC_TYPE");
        if (!docTypeField.isPresent())
            docTypeField = getNode("/ProcessData/ERRORHANDLING_ProcessData/MapResult/Row/DOC_TYPE");
        if (!docTypeField.isPresent())
            docTypeField = getNode("/ProcessData/ErrArchiveData/Err_doctype");
        if (!docTypeField.isPresent())
            docTypeField = getNode("/ProcessData/ERRORHANDLING_ProcessData/TransactionSetIDCode");
        return docTypeField;
    }
    public void extractMultipleField(MetaDataDTO metaDataDTO) {
        try {

            Optional<String> processIdOp = dualExtract();
            if (processIdOp.isPresent()) {
                metaDataDTO.setProcessId(processIdOp.get());
            }
            Optional<String> clusterIdOp = getNode("/ProcessData/ClusterID");
            if(!clusterIdOp.isPresent()){
                clusterIdOp = getNode("/ProcessData/ERRORHANDLING_ProcessData/ClusterID");
            }
            if (clusterIdOp.isPresent()) {
                metaDataDTO.setClusterId(clusterIdOp.get());
            }

            // Extracting EdiKeys
            extractEdiKeys(metaDataDTO);

            Set<String> set = new LinkedHashSet<>();
            List<String> docKeyList = getNodes("/ProcessData/MapResult/Row[*]/DocKey1");
            if (docKeyList.size() > 0) {
                // Remove duplicates
                set.addAll(docKeyList);
                docKeyList.clear();
            }
            metaDataDTO.setDocKeys(set);

            Optional<String> senderField =  getNode("/ProcessData/MapResult/Row/SENDER");
            if (!senderField.isPresent())
                senderField = getNode("/ProcessData/SENDER_ID");
            if (!senderField.isPresent())
                senderField = getNode("/ProcessData/ErrArchiveData/Err_sender");
            if (!senderField.isPresent())
                senderField = getNode("/ProcessData/ERRORHANDLING_ProcessData/MapResult/Row/SENDER");
            if (!senderField.isPresent())
                senderField = getInterchangeSenderID();
            if(!senderField.isPresent())
                senderField = getNode("/ProcessData/MapResult/Row/UCR_INFORMATION/Trading_Partner");
            if(!senderField.isPresent())
                senderField = getNode("/ProcessData/MapResult/Row/TPR_ID");
            if (senderField.isPresent()) {
                metaDataDTO.setSender(senderField.get());
            }

            Optional<String> interchangeSenderId = getInterchangeSenderID();
            if (interchangeSenderId.isPresent()) {
                metaDataDTO.setIsaSndId(interchangeSenderId.get());
            }


            Optional<String> receiverField = getNode("/ProcessData/MapResult/Row/RECEIVER");
            if (!receiverField.isPresent())
                receiverField = getNode("/ProcessData/RECEIVER_ID");
            if (!receiverField.isPresent())
                receiverField = getNode("/ProcessData/ErrArchiveData/Err_receiver");
            if(!receiverField.isPresent())
                receiverField = getNode("/ProcessData/ERRORHANDLING_ProcessData/MapResult/Row/RECEIVER");
            if (!receiverField.isPresent())
                receiverField = getInterchangeReceiverID();
            if (receiverField.isPresent()) {
                metaDataDTO.setReceiver(receiverField.get());
            }

            Optional<String> interchangereceiverId = getInterchangeReceiverID();
            if (interchangereceiverId.isPresent()) {
                metaDataDTO.setIsaRcvId(interchangereceiverId.get());
            }

            Optional<String> docAltTypeField2 =getDocTypeAlternate();
            Optional<String> docTypeField = getDocType();
              if (docTypeField.isPresent()) {
                metaDataDTO.setDocType(docTypeField.get());
            } else if (docAltTypeField2.isPresent() && docAltTypeField2.get().equals("997")) {
                metaDataDTO.setDocType(docAltTypeField2.get());
            }

            Optional<String> directionField = getNode("/ProcessData/MapResult/Row/DIRECTION");
            if (!directionField.isPresent())
                directionField = getNode("/ProcessData/ErrArchiveData/Err_direction");
            if (!directionField.isPresent())
                directionField = getNode("/ProcessData/ERRORHANDLING_ProcessData/MapResult/Row/DIRECTION");
            if (directionField.isPresent()) {
                metaDataDTO.setDirection(directionField.get());
                if (directionField.get().equalsIgnoreCase("OUTBOUND") && receiverField.isPresent()) {
                    metaDataDTO.setTradingPartner(receiverField.get());
                } else if (directionField.get().equalsIgnoreCase("INBOUND") || !directionField.isPresent()) {
                    if (senderField.isPresent()) {
                        metaDataDTO.setTradingPartner(senderField.get());
                    } else if (interchangeSenderId.isPresent()) {
                        metaDataDTO.setTradingPartner(interchangeSenderId.get());
                    }
                }
            }
            else{
                if (senderField.isPresent()) {
                    metaDataDTO.setTradingPartner(senderField.get());
                }
            }

            Optional<String> iccNumber = getNode("ProcessData/InterchangeControlNumber");
            if (!iccNumber.isPresent())
                iccNumber = getNode("/ProcessData/InterchangeInfo/InterchangeControlNumber");
            if (!iccNumber.isPresent())
                iccNumber = getNode("/ProcessData/ERRORHANDLING_ProcessData/InterchangeControlNumber");
            if (!iccNumber.isPresent())
                iccNumber = getNode("/ProcessData/InterchangeInfo/InterchangeControlNumber");
            if (iccNumber.isPresent()) {
                metaDataDTO.setIsaBatCtlNbr(iccNumber.get());
            }

            Optional<String> GS_SND_ID = getNode("/ProcessData/GroupApplicationSenderCode");
            if (!GS_SND_ID.isPresent())
                GS_SND_ID = getNode("/ProcessData/GroupInfo/GroupApplicationSenderCode");
            if (!GS_SND_ID.isPresent())
                GS_SND_ID = getNode("/ProcessData/ERRORHANDLING_ProcessData/GroupApplicationSenderCode");
            if (GS_SND_ID.isPresent()) {
                metaDataDTO.setGsSndId(GS_SND_ID.get());
            }

            Optional<String> GS_RCV_ID = getNode("/ProcessData/GroupApplicationReceiverCode");
            if (!GS_RCV_ID.isPresent())
                GS_RCV_ID = getNode("/ProcessData/GroupInfo/GroupApplicationReceiverCode");
            if (!GS_RCV_ID.isPresent())
                GS_RCV_ID = getNode("/ProcessData/ERRORHANDLING_ProcessData/GroupApplicationReceiverCode");
            if (GS_RCV_ID.isPresent()) {
                metaDataDTO.setGsRcvId(GS_RCV_ID.get());
            }

            Optional<String> GS_GRP_CTL_NBR = getNode("/ProcessData/GroupControlNumber");
            if (!GS_GRP_CTL_NBR.isPresent())
                GS_GRP_CTL_NBR = getNode("/ProcessData/GroupInfo/GroupControlNumber");
            if (!GS_GRP_CTL_NBR.isPresent())
                GS_GRP_CTL_NBR = getNode("/ProcessData/ERRORHANDLING_ProcessData/GroupControlNumber");
            if (GS_GRP_CTL_NBR.isPresent()) {
                metaDataDTO.setGsGrpCtlNbr(GS_GRP_CTL_NBR.get());
            }

            Optional<String> TransactionSetControlNumber = getNode("/ProcessData/TransactionSetControlNumber");
            if (!TransactionSetControlNumber.isPresent())
                TransactionSetControlNumber = getNode("/ProcessData/TransationInfo/TransactionSetControlNumber");
            if (!TransactionSetControlNumber.isPresent())
                TransactionSetControlNumber = getNode("/ProcessData/ERRORHANDLING_ProcessData/TransactionSetControlNumber");
            if (TransactionSetControlNumber.isPresent()) {
                metaDataDTO.setStCtlNbr(TransactionSetControlNumber.get());
            }

            Optional<String> ACK_DOC_TYPE = getNode("/ProcessData/FunctionalAcknowledgment/DetailInformation/AckDocumentType");
            if (ACK_DOC_TYPE.isPresent()){
                metaDataDTO.setAckDocType(ACK_DOC_TYPE.get());
            }

            Optional<String> ACK_DOC_GS_NBR = getNode("/ProcessData/FunctionalAcknowledgment/DetailInformation/AckDocumentGSNumber");
            if (ACK_DOC_GS_NBR.isPresent()){
                metaDataDTO.setAckDocGsNbr(ACK_DOC_GS_NBR.get());
            }

            Optional<String> WMQ_CORRELATION_ID = getNode("/ProcessData/WMQ_correlationId");
            if (WMQ_CORRELATION_ID.isPresent()){
                metaDataDTO.setWmqCorrelationId(WMQ_CORRELATION_ID.get());
            }

            Optional<String> ERR_CRT_S = getNode("/ProcessData/CurDate");
            if (ERR_CRT_S.isPresent()){
                metaDataDTO.setErrorCRT_S(ERR_CRT_S.get());
            }


            LocalDateTime now =LocalDateTime.now();
            ZonedDateTime time = ZonedDateTime.of(now,(ZoneId.of("America/Chicago")));
            metaDataDTO.setTimestamp(time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            metaDataDTO.setTimestampMillis(String.valueOf(time.toInstant().toEpochMilli()));

        } catch (XPathExpressionException e) {
            log.error("{}: >>> DocumentParsingError: XPathExpressionException  <<<", sciObjectId, e);

        }
    }
    public Optional<String> getInterchangeSenderID(){
        Optional<String> interchangeSenderId;
        try {
            interchangeSenderId = getNode("/ProcessData/InterchangeSenderID");
            if (!interchangeSenderId.isPresent())
                interchangeSenderId = getNode("/ProcessData/MapResult/Row/SENDER/InterchangeSenderID");
            if (!interchangeSenderId.isPresent())
                interchangeSenderId = getNode("/ProcessData/InterchangeInfo/InterchangeSenderID");
            if (!interchangeSenderId.isPresent())
                interchangeSenderId = getNode("/ProcessData/ERRORHANDLING_ProcessData/InterchangeSenderID");
            if (interchangeSenderId.isPresent()) {
                return interchangeSenderId;
            }
            return Optional.empty();
        } catch (XPathExpressionException e) {
            log.error("context", e);
        }
        return Optional.empty();
    }

    public Optional<String> getInterchangeReceiverID(){
        Optional<String> interchangereceiverId;
        try {
            interchangereceiverId = getNode("/ProcessData/InterchangeReceiverID");
            if (!interchangereceiverId.isPresent())
                interchangereceiverId = getNode("/ProcessData/InterchangeInfo/InterchangeReceiverID");
            if (!interchangereceiverId.isPresent())
                interchangereceiverId = getNode("/ProcessData/MapResult/Row/RECEIVER/InterchangeReceiverID");
            if (!interchangereceiverId.isPresent())
                interchangereceiverId = getNode("/ProcessData/ERRORHANDLING_ProcessData/InterchangeReceiverID");
            if(interchangereceiverId.isPresent()){
                return interchangereceiverId;
            }
            return Optional.empty();
        } catch (XPathExpressionException e) {
            log.error("context", e);
        }
        return Optional.empty();
    }
}
