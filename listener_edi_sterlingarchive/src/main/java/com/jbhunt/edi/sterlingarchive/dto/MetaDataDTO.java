package com.jbhunt.edi.sterlingarchive.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class MetaDataDTO implements Serializable {

    private static final long serialVersionUID = -7261578066957123328L;
    private String docId = "";
    private String ediDocHdrI = "";
    private String processId = "";
    private String clusterId = "";
    private Set<String> ediKeys;
    private String ediKey;
    private Set<String> docKeys;
    private String sender = "";
    private String receiver = "";
    private String docType = "";
    private String direction = "";
    private String tradingPartner = "";
    private String isaSndId = "";
    private String isaRcvId = "";
    private String isaBatCtlNbr = "";
    private String gsSndId = "";
    private String gsRcvId = "";
    private String gsGrpCtlNbr = "";
    private String stCtlNbr = "";
    private String docCount = "";
    private String timestamp = "";
    private String timestampMillis = "";
    private String source = "";
    private String errorMessage = "";
    private String ackDocType = "";
    private String ackDocGsNbr ="";
    private String insertedInstant = "";
    private boolean error ;
    private String docTypeToInsert ="";
    private String gsGrpCtrlNbrToInsert ="";
    private String wmqCorrelationId = "";
    private String errorCRT_S = "";
    private String processStatus = "";
    private DocumentReferenceDTO ediRawDocRef ;
    private DocumentReferenceDTO adfDocRef ;
    private DocumentReferenceDTO processDataDocRef;
    private DocumentReferenceDTO translationErrorDocRef;

}
