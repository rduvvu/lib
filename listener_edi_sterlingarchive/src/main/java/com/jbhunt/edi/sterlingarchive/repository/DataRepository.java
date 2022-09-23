package com.jbhunt.edi.sterlingarchive.repository;

import com.jbhunt.edi.sterlingarchive.dto.MetaDataDTO;
import com.jbhunt.edi.sterlingarchive.dto.NineNineSevenResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jbhunt.edi.sterlingarchive.constants.SterlingArchiveConstants.*;

@Slf4j
@Repository("dataRepository")
public class DataRepository {

    NamedParameterJdbcTemplate ediJdbcTemplate;

    @Autowired
    public DataRepository( NamedParameterJdbcTemplate ediJdbcTemplate) {
        this.ediJdbcTemplate = ediJdbcTemplate;
    }

    public void insertToNewEDITable(MetaDataDTO ediData, String refName ) throws ParseException {
        //INBOUND will be docType and RAW for pre and post data
        //OUTBOUND will be RAW and docType for pre and post data
        String sql = "INSERT INTO [JBH].[EDI_DOC_HDR] \n" +
                "   (SND_ID ,\n" +
                "       RCV_ID ,\n" +
                "       DOC_TYP ,\n" +
                "       DOC_DIR_C ,\n" +
                "       TPR_NBR ,\n" +
                "       PRS_I ,\n" +
                "       FIL_NM ,\n" +
                "       EDI_DAT ,\n" +
                "       CRT_S ,\n" +
                "       CRT_UID ,\n" +
                "       CRT_PGM_C ,\n" +
                "       LST_UPD_S ,\n" +
                "       LST_UPD_UID ,\n" +
                "       LST_UPD_PGM_C ,\n" +
                "       PRS_STT ,\n" +
                "       ISA_SND_ID ,\n" +
                "       ISA_RCV_ID ,\n" +
                "       ISA_BAT_CTL_NBR ,\n" +
                "       GS_SND_ID ,\n" +
                "       GS_RCV_ID ,\n" +
                "       GS_GRP_CTL_NBR ,\n" +
                "       EDI_DAT_DOC_Q) \n" +
                " VALUES ( :snd_id  \n" +
                "                 , :rcv_id  \n" +
                "                 , :doc_typ  \n" +
                "                 , :doc_dir_c  \n" +
                "                 , :tpr_nbr  \n" +
                "                 , :prs_i  \n" +
                "                 , :fil_nm  \n" +
                "                 , :edi_dat  \n" +
                "                 , :crt_s  \n" +
                "                 , :crt_uid  \n" +
                "                 , :crt_pgm_c  \n" +
                "                 , :lst_upd_s  \n" +
                "                 , :lst_upd_uid  \n" +
                "                 , :lst_upd_pgm_c  \n" +
                "                 , :prs_stt  \n" +
                "                 , :isa_snd_id  \n" +
                "                 , :isa_rcv_id  \n" +
                "                 , :isa_bat_ctl_nbr  \n" +
                "                 , :gs_snd_id  \n" +
                "                 , :gs_rcv_id  \n" +
                "                 , :gs_grp_ctl_nbr  \n" +
                "                 , :edi_dat_doc_q );" ;

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("edi_doc_hdr_i", "");
        queryParams.put("snd_id", ediData.getSender());
        queryParams.put("rcv_id", ediData.getReceiver());
        queryParams.put("doc_typ", ediData.getDocTypeToInsert());
        queryParams.put("doc_dir_c", ediData.getDirection());
        queryParams.put("tpr_nbr", ediData.getTradingPartner());
        queryParams.put("prs_i", ediData.getProcessId());
        queryParams.put("fil_nm", null);
        queryParams.put("edi_dat", refName);
        queryParams.put("crt_s", ediData.getInsertedInstant());
        queryParams.put("crt_uid", ediData.getClusterId());
        queryParams.put("lst_upd_s", ediData.getInsertedInstant());
        queryParams.put("crt_pgm_c", "Java archive listener");
        queryParams.put("lst_upd_uid", ediData.getClusterId());
        queryParams.put("lst_upd_pgm_c", "Java archive listener");
        queryParams.put("prs_stt", ediData.getProcessStatus());
        queryParams.put("isa_snd_id", ediData.getIsaSndId());
        queryParams.put("isa_rcv_id", ediData.getIsaRcvId());
        queryParams.put("isa_bat_ctl_nbr", ediData.getIsaBatCtlNbr());
        queryParams.put("gs_snd_id", ediData.getGsSndId());
        queryParams.put("gs_rcv_id", ediData.getGsRcvId());
        queryParams.put("gs_grp_ctl_nbr", ediData.getGsGrpCtlNbr());
        queryParams.put("edi_dat_doc_q", ediData.getDocCount());

        ediJdbcTemplate.update(sql, queryParams);
    }

    public String selectEdiDocHdrI(MetaDataDTO ediData){
        String sql = "SELECT [EDI_DOC_HDR_I]\n" +
                "  FROM [JBH].[EDI_DOC_HDR] A\n" +
                "  WHERE A.PRS_I = :prs_i\n" +
                "  AND A.CRT_S = :crt_s   \n" +
                "  AND A.DOC_TYP = :doc_typ     \n" +
                "  AND A.CRT_UID = :crt_uid      ";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("prs_i", ediData.getProcessId());
        queryParams.put("crt_s", ediData.getInsertedInstant());
        queryParams.put("crt_uid", ediData.getClusterId());
        queryParams.put("doc_typ", ediData.getDocTypeToInsert());
        List<String> result = ediJdbcTemplate.query(sql, queryParams, (rs, rowNum) -> rs.getString(1));
        if(result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    public List<NineNineSevenResponseDTO> selectNineNineSevenMetaData(MetaDataDTO ediData){
        String sql = "SELECT EDI_DAT as uUID, EDI_DOC_HDR_I as ediDocHdrI \n" +
                "  FROM [JBH].[EDI_DOC_HDR] \n" +
                "  where ISA_SND_ID = :isa_snd_id \n" +
                "  AND ISA_RCV_ID = :isa_rcv_id \n" +
                "  AND DOC_TYP = :docType \n" +
                "  AND GS_GRP_CTL_NBR = :gs_grp_ctl_nbr \n" +
                "  AND GS_SND_ID = :gs_snd_id \n" +
                "  AND GS_RCV_ID = :gs_rcv_id";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("isa_snd_id", ediData.getIsaRcvId());
        queryParams.put("isa_rcv_id", ediData.getIsaSndId());
        queryParams.put("docType", ediData.getDocTypeToInsert());
        queryParams.put("gs_grp_ctl_nbr", ediData.getGsGrpCtrlNbrToInsert());
        queryParams.put("gs_snd_id", ediData.getGsRcvId());
        queryParams.put("gs_rcv_id", ediData.getGsSndId());

        List<String> result = ediJdbcTemplate.query(sql, queryParams, (rs, rowNum) -> rs.getString(1));
        final BeanPropertyRowMapper<NineNineSevenResponseDTO> mapper =
                new BeanPropertyRowMapper<>(NineNineSevenResponseDTO.class);
        List<NineNineSevenResponseDTO> list = ediJdbcTemplate.query(sql, queryParams, mapper);
        if(list.isEmpty() ) {
            throw new IllegalStateException("997 response query did not return any rows.");
        }
        return list;
    }

    public void insertToNewEDIIDXTable(MetaDataDTO ediData, String ediDocHDRI, String idxTyp, String idxVal ) throws ParseException {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-DD hh:mm:ss.SSSSSSS");
        String sql ="INSERT INTO [JBH].[EDI_DOC_IDX]\n" +
                "       ([EDI_DOC_HDR_I]\n" +
                "      ,[IDX_TYP]\n" +
                "      ,[IDX_VAL]\n" +
                "      ,[CRT_S]\n" +
                "      ,[CRT_UID]\n" +
                "      ,[CRT_PGM_C]\n" +
                "      ,[LST_UPD_S]\n" +
                "      ,[LST_UPD_UID]\n" +
                "      ,[LST_UPD_PGM_C]\n" +
                "      ,[XAC_SET_CTL_NBR])\n" +
                "       VALUES(:edi_doc_hdr_i\n" +
                "      ,:idx_typ\n" +
                "      ,:idx_val\n" +
                "      ,:crt_s\n" +
                "      ,:crt_uid\n" +
                "      ,:crt_pgm_c\n" +
                "      ,:lst_upd_s  \n" +
                "      ,:lst_upd_uid\n" +
                "      ,:lst_upd_pgm_c\n" +
                "      ,:gs_grp_ctl_nbr);";

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("edi_doc_hdr_i", ediDocHDRI);
        queryParams.put("idx_typ", idxTyp);
        queryParams.put("idx_val",idxVal);
        queryParams.put("crt_s",ediData.getInsertedInstant());
        queryParams.put("lst_upd_s",ediData.getInsertedInstant());
        queryParams.put("crt_pgm_c", "Java archive listener");
        queryParams.put("lst_upd_uid",  ediData.getClusterId());
        queryParams.put("lst_upd_pgm_c","Java archive listener");
        queryParams.put("gs_grp_ctl_nbr", ediData.getGsGrpCtlNbr());
        queryParams.put("crt_uid", ediData.getClusterId());

        ediJdbcTemplate.update(sql, queryParams);
    }

    public void insertToCompleteOrigDoc(String ediDocHDRI){
        String sql = "UPDATE [JBH].[EDI_DOC_HDR]\n" +
                "SET PRS_STT = 'COMPLETE'\n" +
                "where EDI_DOC_HDR_I = :edi_doc_hdr_i";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("edi_doc_hdr_i", ediDocHDRI);
        ediJdbcTemplate.update(sql, queryParams);
    }

    public void insertToNewErrMsgLogTable(MetaDataDTO ediData, String refName ) throws ParseException {
        String sql ="INSERT INTO [JBH].[EDI_ERR_MSG_LOG]\n" +
                "      ([EDI_DOC_HDR_I]\n" +
                "      ,[ERR_MSG]\n" +
                "      ,[EDI_XLT_DAT_XML]\n" +
                "      ,[RTRY_CNT_Q]\n" +
                "      ,[ERR_CRT_S]\n" +
                "      ,[PRS_STT]\n" +
                "      ,[REC_STT]\n" +
                "      ,[CRT_S]\n" +
                "      ,[CRT_UID]\n" +
                "      ,[CRT_PGM_C]\n" +
                "      ,[LST_UPD_S]\n" +
                "      ,[LST_UPD_UID]\n" +
                "      ,[LST_UPD_PGM_C])\n" +
                "  VALUES (:edi_doc_hdr_i,\n" +
                "  :err_msg,\n" +
                "  :ref_name,\n" +
                "  :rtry_cnt,\n" +
                "  :err_crt_s,\n" +
                "  :err_prs_stt,\n" +
                "  :rec_stt,\n" +
                "  :crt_s,\n" +
                "  :crt_uid,\n" +
                "  :crt_pgm_c,\n" +
                "  :lst_upd_s,\n" +
                "  :lst_upd_uid,\n" +
                "  :lst_upd_pgm_c);";

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("edi_doc_hdr_i", ediData.getEdiDocHdrI());
        queryParams.put("err_msg", ediData.getErrorMessage());
        queryParams.put("ref_name",refName);
        queryParams.put("rtry_cnt",RTRY_CNT_Q);
        queryParams.put("crt_s",ediData.getInsertedInstant());
        queryParams.put("err_crt_s",ediData.getErrorCRT_S());
        queryParams.put("err_prs_stt",ERROR_PRS_STT);
        queryParams.put("rec_stt",REC_STT);
        queryParams.put("lst_upd_s",ediData.getInsertedInstant());
        queryParams.put("crt_pgm_c", "Java archive listener");
        queryParams.put("lst_upd_uid",  ediData.getClusterId());
        queryParams.put("lst_upd_pgm_c",CRT_PGM_C);
        queryParams.put("crt_uid", ediData.getClusterId());

        ediJdbcTemplate.update(sql, queryParams);
    }

}
