package com.jbhunt.edi.sterlingarchive.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocumentReferenceDTO {

        private String reference = "";
        private String path = "";
        public DocumentReferenceDTO(String reference,String path){
            this.reference = reference;
            this.path = path;
        }

}
