package com.jbhunt.edi.sterlingarchive.repository;

import com.jbhunt.edi.sterlingarchive.dto.MetaDataDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class DataRepositoryTest {
    @Mock
    private NamedParameterJdbcTemplate ediJdbcTemplate;

    @InjectMocks
    private DataRepository dataRepository;

    @Test
    public void insertToNewEDIIDXTable() throws ParseException {
        MetaDataDTO metadata = MetaDataDTO.builder().build();
        metadata.setDocType("204TEST");
        metadata.setDirection("INBOUNDTEST");
        dataRepository.insertToNewEDIIDXTable(metadata, "TEST", "TEST", "TEST");
    }

    @Test
    public void insertToNewEDITable() throws ParseException {
        MetaDataDTO metadata = MetaDataDTO.builder().build();
        metadata.setDocType("204TEST");
        metadata.setDirection("INBOUNDTEST");
        dataRepository.insertToNewEDITable(metadata, "TEST");
    }

    @Test
    public void selectEDIDOCHDRITest() throws ParseException {
        MetaDataDTO metadata = MetaDataDTO.builder().build();
        metadata.setDocType("204TEST");
        metadata.setDirection("INBOUNDTEST");
        dataRepository.selectEdiDocHdrI(metadata);
    }

    @Test
    public void insertToNewErrMsgLogTable() throws ParseException {
        MetaDataDTO metadata = MetaDataDTO.builder().build();
        metadata.setDocType("204TEST");
        metadata.setDirection("INBOUNDTEST");
        dataRepository.insertToNewErrMsgLogTable(metadata, "TEST");
    }

    @Test(expected=IllegalStateException.class)
    public void selectNineNineSevenTest() throws IllegalStateException {
        MetaDataDTO metadata = MetaDataDTO.builder().build();
        metadata.setEdiDocHdrI("204TEST");
        metadata.setIsaRcvId("INBOUNDTEST");
        metadata.setIsaSndId("TEST");
        metadata.setGsGrpCtrlNbrToInsert("TEST");
        metadata.setGsRcvId("TEST");
        metadata.setGsSndId("TEST");
        dataRepository.selectNineNineSevenMetaData(metadata);
    }

    @Test
    public void insertToCompleteOrigDoc() throws ParseException {
        dataRepository.insertToCompleteOrigDoc("TEST");
    }
}
