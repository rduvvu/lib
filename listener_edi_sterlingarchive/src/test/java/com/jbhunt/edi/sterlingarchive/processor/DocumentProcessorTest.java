package com.jbhunt.edi.sterlingarchive.processor;

import com.jbhunt.edi.sterlingarchive.dto.MetaDataDTO;
import com.jbhunt.edi.sterlingarchive.dto.NineNineSevenResponseDTO;
import com.jbhunt.edi.sterlingarchive.repository.DataRepository;
import com.jbhunt.edi.sterlingarchive.utils.CloudStorageUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentProcessorTest {

    @InjectMocks
    private DocumentProcessor documentProcessor;

    @Mock
    private CloudStorageUtil cloudStorageUtil;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private Exchange exchange;

    @Mock
    private Message messageIn;

    @Before
    public void before() {
        documentProcessor = new DocumentProcessor(dataRepository, cloudStorageUtil);
    }

    @Test
    public void documentProcessorTestAsync() throws Exception {
          when(exchange.getIn()).thenReturn(messageIn);
          when(messageIn.getBody(String.class)).thenReturn(getXmlString());
          doNothing().when(cloudStorageUtil).uploadEDIDoc(anyString(), anyString(), anyString(), anyString());
          documentProcessor.process(exchange, b -> {
          });
          verify(exchange, times(0))
                  .setProperty(eq(Exchange.EXCEPTION_CAUGHT), any());
    }

    @Test
    public void documentProcessorTestAsyncOutbound() throws Exception {
        when(exchange.getIn()).thenReturn(messageIn);
        when(messageIn.getBody(String.class)).thenReturn(getXmlStringOutboundPre());
        doNothing().when(cloudStorageUtil).uploadEDIDoc(anyString(), anyString(), anyString(), anyString());
        documentProcessor.process(exchange, b -> {
        });
        verify(exchange, times(0))
                .setProperty(eq(Exchange.EXCEPTION_CAUGHT), any());
    }

    @Test
    public void documentProcessorTestAsyncSciObjectIDNotFound() throws Exception {
        when(exchange.getIn()).thenReturn(messageIn);
        when(messageIn.getBody(String.class)).thenReturn(getXMLStringwithNosciObjectID());
        doNothing().when(cloudStorageUtil).uploadEDIDoc(anyString(), anyString(), anyString(), anyString());
        documentProcessor.process(exchange, b -> {
        });
        verify(exchange, times(1))
                .setProperty(eq(Exchange.EXCEPTION_CAUGHT), any());
    }

    @Test
    public void documentProcessorTestAsyncEmptyBody() throws Exception {
        when(exchange.getIn()).thenReturn(messageIn);
        when(messageIn.getBody(String.class)).thenReturn("");
        doNothing().when(cloudStorageUtil).uploadEDIDoc(anyString(), anyString(), anyString(), anyString());
        documentProcessor.process(exchange, b -> {
        });
        verify(exchange, times(1))
                .setProperty(eq(Exchange.EXCEPTION_CAUGHT), any());
    }

    @Test
    public void documentProcessorTestAsyncErrorFlag() throws Exception {
        when(exchange.getIn()).thenReturn(messageIn);
        when(messageIn.getBody(String.class)).thenReturn(getXmlStringWithErrorFlag());
        doNothing().when(cloudStorageUtil).uploadEDIDoc(anyString(), anyString(), anyString(), anyString());
        documentProcessor.process(exchange, b -> {
        });
        verify(exchange, times(0))
                .setProperty(eq(Exchange.EXCEPTION_CAUGHT), any());
    }

    @Test
    public void documentProcessorTestAsyncErrorFlagAdvStatusMessage() throws Exception {
        when(exchange.getIn()).thenReturn(messageIn);
        when(messageIn.getBody(String.class)).thenReturn(getXmlStringWithErrorFlagADVStatusMsg());
        doNothing().when(cloudStorageUtil).uploadEDIDoc(anyString(), anyString(), anyString(), anyString());
        documentProcessor.process(exchange, b -> {
        });
        verify(exchange, times(0))
                .setProperty(eq(Exchange.EXCEPTION_CAUGHT), any());
    }

    @Test
    public void documentProcessorTestAsync997() throws Exception {
        when(exchange.getIn()).thenReturn(messageIn);
        when(messageIn.getBody(String.class)).thenReturn(getXmlStringwith997AckData());
        NineNineSevenResponseDTO dto = new NineNineSevenResponseDTO();
        dto.setEdiDocHdrI("124");
        dto.setUUID("1257");
        List<NineNineSevenResponseDTO> mylist = new ArrayList<>();
        mylist.add(dto);
        when(dataRepository.selectNineNineSevenMetaData(any())).thenReturn(mylist);

        doNothing().when(cloudStorageUtil).uploadEDIDoc(anyString(), anyString(), anyString(), anyString());
        documentProcessor.process(exchange, b -> {
        });
        verify(exchange, times(0))
                .setProperty(eq(Exchange.EXCEPTION_CAUGHT), any());
    }

    @Test
    public void documentProcessorTestSync() {
        documentProcessor.process(exchange);
    }

    public String getXmlStringWithErrorFlag() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ProcessData><MapResult><Row>\n" +
                "</Row>\n" +
                "</MapResult>\n" +
                "<myWorkflowID>12624696</myWorkflowID>\n" +
                "<CurDate>12624696</CurDate>\n" +
                "<PreDataFile SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945101'/>\n" +
                "<ERRORHANDLING_ProcessData>" +
                "<stat_rpt>some error has occured</stat_rpt>" +
                "<ERROR_SERVICE>" +
                "<SERVICE_NAME>12345678</SERVICE_NAME>\n" +
                "<InterchangeControlNumber>12345678</InterchangeControlNumber>\n" +
                "</ERROR_SERVICE>" +
                "</ERRORHANDLING_ProcessData>" +
                "<FunctionalAcknowledgment>" +
                "<DetailInformation>" +
                "<AckDocumentType>12345678</AckDocumentType>\n" +
                "<AckDocumentGSNumber>12345678</AckDocumentGSNumber>\n" +
                "</DetailInformation>\n" +
                "</FunctionalAcknowledgment>\n" +
                "</ProcessData>";

    }
    public String getXmlStringWithErrorFlagADVStatusMsg() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ProcessData>><MapResult><Row>\n" +
                "<DOC_TYPE>204</DOC_TYPE>\n" +
                "</Row>\n" +
                "</MapResult>\n" +
                "<myWorkflowID>12624696</myWorkflowID>\n" +
                "<PreDataFile SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945101'/>\n" +
                "<ERRORHANDLING_ProcessData>" +
                "<InterchangeControlNumber>12624696</InterchangeControlNumber>\n" +
                "<MapResult><Row>\n" +
                "<DOC_TYPE>204</DOC_TYPE>\n" +
                "<Correlation_ID>204</Correlation_ID>\n" +
                "</Row>\n" +
                "</MapResult>\n" +
                "<stat_rpt>some error has occured</stat_rpt>" +
                "<ERROR_SERVICE>" +
                "<ADV_STATUS>12345678</ADV_STATUS>\n" +
                "</ERROR_SERVICE>" +
                "<CurDate>12345678</CurDate>\n" +
                "</ERRORHANDLING_ProcessData>" +
                "</ProcessData>";

    }

    public String getXMLStringwithNosciObjectID(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ProcessData>\n" +
                "<RECEIVER>HJCS</RECEIVER>\n" +
                "<SENDER>AMAZON</SENDER>\n" +
                "</ProcessData>\n";
    }

    public String getXmlString() {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ProcessData>  " +
                "<Correlation_ID>2123404</Correlation_ID>\n" +
                "<TransactionSetIDCode>2123404</TransactionSetIDCode>\n" +
                "<WMQ_correlationId>C3E2D840D4D8E3F14040404040404040DAE8CC3E24339402</WMQ_correlationId><MapResult><Row><XLT_MAP_I>17896</XLT_MAP_I>\n" +
                "<RECEIVER>HJCS</RECEIVER>\n" +
                "<SENDER>AMAZON</SENDER>\n" +
                "<MAP_NAME>JBH_AMAZOND_204_RECV_4010</MAP_NAME>\n" +
                "<TRANS_ENDPOINT_MBX>/APP/MQ/INBOUND</TRANS_ENDPOINT_MBX>\n" +
                "<DIRECTION>INBOUND</DIRECTION>\n" +
                "<DOC_EXT_MAP></DOC_EXT_MAP>\n" +
                "<SKIP_HEADER_REC></SKIP_HEADER_REC>\n" +
                "<FILE_FORMAT_TYPE></FILE_FORMAT_TYPE>\n" +
                "</Row>\n" +
                "<Row><JBH_TPR_CMN_DST_I>25218</JBH_TPR_CMN_DST_I>\n" +
                "<DST_TYP>BP</DST_TYP>\n" +
                "<RMT_HOST_ADR>QMT13.JBHUNT.COM</RMT_HOST_ADR>\n" +
                "<PORT_NBR>1414</PORT_NBR>\n" +
                "<DST_ADR>EDI.A.SI.APP.INBOUND</DST_ADR>\n" +
                "<MQ_CHL>SI.CLIENT1</MQ_CHL>\n" +
                "<XNL_PWD></XNL_PWD>\n" +
                "<CUSTOM_PROCESS_NAME>JBH_BP_CUSTOM_204_ADF_NEXT_ONBOARD</CUSTOM_PROCESS_NAME>\n" +
                "<DESTINATION_SYSTEM>Mainframe</DESTINATION_SYSTEM>\n" +
                "<MQ_HEADER_F>Y</MQ_HEADER_F>\n" +
                "<JBH_TPR_CMN_DST_I>46940</JBH_TPR_CMN_DST_I>\n" +
                "<DST_TYP>BP</DST_TYP>\n" +
                "<RMT_HOST_ADR>QMT13.JBHUNT.COM</RMT_HOST_ADR>\n" +
                "<PORT_NBR>1414</PORT_NBR>\n" +
                "<DST_ADR>EDI.A.ORDER.MGMT.INBOUND.PROF2</DST_ADR>\n" +
                "<MQ_CHL>SI.CLIENT1</MQ_CHL>\n" +
                "<XNL_PWD></XNL_PWD>\n" +
                "<CUSTOM_PROCESS_NAME>JBH_BP_CUSTOM_204_ADF_NEXT_ONBOARD</CUSTOM_PROCESS_NAME>\n" +
                "<DESTINATION_SYSTEM>OM</DESTINATION_SYSTEM>\n" +
                "<MQ_HEADER_F>N</MQ_HEADER_F>\n" +
                "</Row>\n" +
                "<Row><Correlation_ID>113K2H66R</Correlation_ID>\n" +
                "<Correlation_ID>B-3DW8XXXGB</Correlation_ID>\n" +
                "<UCR_INFORMATION><Trading_Partner>AMAZOND</Trading_Partner>\n" +
                "</UCR_INFORMATION>\n" +
                "<Correlation_ID>113K2H66R</Correlation_ID>\n" +
                "<TPR_ID>AMAZOND</TPR_ID>\n" +
                "<DocKey1>CHANGE</DocKey1>\n" +
                "</Row>\n" +
                "</MapResult>\n" +
                "<Environment>SI60TESTA</Environment>\n" +
                "<ClusterID>SI60_A</ClusterID>\n" +
                "<myWorkflowID>12624696</myWorkflowID>\n" +
                "<ParentBP>JBH_BP_CUSTOM_204_ADF_NEXT_ONBOARD</ParentBP>\n" +
                "<stat_rpt>service Translation doesn&apos;t have status report</stat_rpt>\n" +
                "<PostDataFile SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945442'/>\n" +
                "<PreDataFile SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945101'/>\n" +
                "<ProcessDataNodeName>PostData</ProcessDataNodeName>\n" +
                "<PreData>![CDATA[ISA*00*          *00*          *ZZ*AMAZON         *02*HJCS           *210709*1742*U*00401*000334739*0*P*&gt;~GS*SM*AMAZON*HJCS*20210709*174259*334739*X*004010~ST*204*0001~B2**HJCS**113K2H66R**PP*Q~B2A*04*P ~L11*B-3DW8XXXGB*RSN~L11*T-115WQCQ8J*LO~L11*1506.0*91~L11*MEDIUM*ZZ~G62*64*20210709*1*1942~MS3*HJCS*B**TL~N7**0*********TF~S5*1*CL~L11*1919454926*BX~G62*37*20210709*I*1051~G62*38*20210709*K*1052~LAD*PLT*0~LAD*CTN*0~LAD*UNT*0~N1*SH*ONT1*ZZ*ONT191752~N3*11200 Iberia Street~N4*MIRA LOMA*CA*91752*US~S5*2*CU~L11*1919454926*BX~G62*53*20210709*G*1157~G62*54*20210709*L*1158~LAD*PLT*0~LAD*CTN*0~LAD*UNT*0~N1*CN*KRB1*ZZ*KRB192408~N3*555 E ORANGE SHOW RD~N4*SAN BERNARDINO*CA*92408*US~L3*****1455~SE*32*0001~GE*1*334739~IEA*1*000334739~\n" +
                "]]</PreData>\n" +
                "<PrimaryDocument SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945442'/>\n" +
                "<PostData>testing 123</PostData>\n" +
                "</ProcessData>\n";
    }

    public String getXmlStringOutboundPre() {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ProcessData><MapResult><Row><XLT_MAP_I>17896</XLT_MAP_I>\n" +
                "<RECEIVER>HJCS</RECEIVER>\n" +
                "<SENDER>AMAZON</SENDER>\n" +
                "<DOC_TYPE>204</DOC_TYPE>\n" +
                "<MAP_NAME>JBH_AMAZOND_204_RECV_4010</MAP_NAME>\n" +
                "<TRANS_ENDPOINT_MBX>/APP/MQ/INBOUND</TRANS_ENDPOINT_MBX>\n" +
                "<DIRECTION>OUTBOUND</DIRECTION>\n" +
                "<DOC_EXT_MAP></DOC_EXT_MAP>\n" +
                "<SKIP_HEADER_REC></SKIP_HEADER_REC>\n" +
                "<FILE_FORMAT_TYPE></FILE_FORMAT_TYPE>\n" +
                "</Row>\n" +
                "<Row><JBH_TPR_CMN_DST_I>25218</JBH_TPR_CMN_DST_I>\n" +
                "<DST_TYP>BP</DST_TYP>\n" +
                "<RMT_HOST_ADR>QMT13.JBHUNT.COM</RMT_HOST_ADR>\n" +
                "<PORT_NBR>1414</PORT_NBR>\n" +
                "<DST_ADR>EDI.A.SI.APP.INBOUND</DST_ADR>\n" +
                "<MQ_CHL>SI.CLIENT1</MQ_CHL>\n" +
                "<XNL_PWD></XNL_PWD>\n" +
                "<CUSTOM_PROCESS_NAME>JBH_BP_CUSTOM_204_ADF_NEXT_ONBOARD</CUSTOM_PROCESS_NAME>\n" +
                "<DESTINATION_SYSTEM>Mainframe</DESTINATION_SYSTEM>\n" +
                "<MQ_HEADER_F>Y</MQ_HEADER_F>\n" +
                "<JBH_TPR_CMN_DST_I>46940</JBH_TPR_CMN_DST_I>\n" +
                "<DST_TYP>BP</DST_TYP>\n" +
                "<RMT_HOST_ADR>QMT13.JBHUNT.COM</RMT_HOST_ADR>\n" +
                "<PORT_NBR>1414</PORT_NBR>\n" +
                "<DST_ADR>EDI.A.ORDER.MGMT.INBOUND.PROF2</DST_ADR>\n" +
                "<MQ_CHL>SI.CLIENT1</MQ_CHL>\n" +
                "<XNL_PWD></XNL_PWD>\n" +
                "<CUSTOM_PROCESS_NAME>JBH_BP_CUSTOM_204_ADF_NEXT_ONBOARD</CUSTOM_PROCESS_NAME>\n" +
                "<DESTINATION_SYSTEM>OM</DESTINATION_SYSTEM>\n" +
                "<MQ_HEADER_F>N</MQ_HEADER_F>\n" +
                "</Row>\n" +
                "<Row><Correlation_ID>113K2H66R</Correlation_ID>\n" +
                "<Correlation_ID>B-3DW8XXXGB</Correlation_ID>\n" +
                "<UCR_INFORMATION><Trading_Partner>AMAZOND</Trading_Partner>\n" +
                "</UCR_INFORMATION>\n" +
                "<Correlation_ID>113K2H66R</Correlation_ID>\n" +
                "<TPR_ID>AMAZOND</TPR_ID>\n" +
                "<DocKey1>CHANGE</DocKey1>\n" +
                "</Row>\n" +
                "</MapResult>\n" +
                "<Environment>SI60TESTA</Environment>\n" +
                "<ClusterID>SI60_A</ClusterID>\n" +
                "<myWorkflowID>12624696</myWorkflowID>\n" +
                "<ParentBP>JBH_BP_CUSTOM_204_ADF_NEXT_ONBOARD</ParentBP>\n" +
                "<stat_rpt>service Translation doesn&apos;t have status report</stat_rpt>\n" +
                "<PostDataFile SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945442'/>\n" +
                "<PreDataFile SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945101'/>\n" +
                "<ProcessDataNodeName>PostData</ProcessDataNodeName>\n" +
                "<PreData>![CDATA[ISA*00*          *00*          *ZZ*AMAZON         *02*HJCS           *210709*1742*U*00401*000334739*0*P*&gt;~GS*SM*AMAZON*HJCS*20210709*174259*334739*X*004010~ST*204*0001~B2**HJCS**113K2H66R**PP*Q~B2A*04*P ~L11*B-3DW8XXXGB*RSN~L11*T-115WQCQ8J*LO~L11*1506.0*91~L11*MEDIUM*ZZ~G62*64*20210709*1*1942~MS3*HJCS*B**TL~N7**0*********TF~S5*1*CL~L11*1919454926*BX~G62*37*20210709*I*1051~G62*38*20210709*K*1052~LAD*PLT*0~LAD*CTN*0~LAD*UNT*0~N1*SH*ONT1*ZZ*ONT191752~N3*11200 Iberia Street~N4*MIRA LOMA*CA*91752*US~S5*2*CU~L11*1919454926*BX~G62*53*20210709*G*1157~G62*54*20210709*L*1158~LAD*PLT*0~LAD*CTN*0~LAD*UNT*0~N1*CN*KRB1*ZZ*KRB192408~N3*555 E ORANGE SHOW RD~N4*SAN BERNARDINO*CA*92408*US~L3*****1455~SE*32*0001~GE*1*334739~IEA*1*000334739~\n" +
                "]]</PreData>\n" +
                "<PrimaryDocument SCIObjectID='JVTB2B10102:node2:17a87a71eee:4945442'/>\n" +
                "<PostData>testing 123</PostData>\n" +
                "</ProcessData>\n";
    }


    public String getXmlStringwith997AckData() {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ProcessData><InterchangeSenderID>008256224</InterchangeSenderID>\n" +
                "<InterchangeReceiverID>HJBT</InterchangeReceiverID>\n" +
                "<InterchangeControlVersionNumber>00401</InterchangeControlVersionNumber>\n" +
                "<InterchangeTestIndicator>T</InterchangeTestIndicator>\n" +
                "<InterchangeControlNumber>000000096</InterchangeControlNumber>\n" +
                "<GroupApplicationSenderCode>008256224</GroupApplicationSenderCode>\n" +
                "<GroupApplicationReceiverCode>HJBT</GroupApplicationReceiverCode>\n" +
                "<GroupControlNumber>76</GroupControlNumber>\n" +
                "<TransactionSetIDCode>997</TransactionSetIDCode>\n" +
                "<BPDATA><WORKFLOW_ID>54103959</WORKFLOW_ID>\n" +
                "</BPDATA>\n" +
                "<ClusterID>SI60_A</ClusterID>\n" +
                "<MapResult><Row><DIRECTION>INBOUND</DIRECTION>\n" +
                "</Row>\n" +
                "</MapResult>\n" +
                "<Correlation_ID>54</Correlation_ID>\n" +
                "<PrimaryDocument SCIObjectID='JVTB2B10102:node2:1827e6758b2:77921963'/>\n" +
                "<stat_rpt><![CDATA[ service Translation doesn't have status report]]></stat_rpt>\n" +
                "<FunctionalAcknowledgment><DetailInformation><AckDocumentType>214</AckDocumentType>\n" +
                "<AckDocumentGSNumber>54</AckDocumentGSNumber>\n" +
                "</DetailInformation>\n" +
                "</FunctionalAcknowledgment>\n" +
                "\n" +
                "<PreData><![CDATA[ISA*00*]]></PreData></ProcessData>";
    }

}
