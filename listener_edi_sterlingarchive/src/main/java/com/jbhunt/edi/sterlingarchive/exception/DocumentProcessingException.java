package com.jbhunt.edi.sterlingarchive.exception;

public class DocumentProcessingException extends RuntimeException {
    public DocumentProcessingException(String s) {
        super(s);
    }

    public DocumentProcessingException(Throwable cause) {
        super(cause);
    }
}
