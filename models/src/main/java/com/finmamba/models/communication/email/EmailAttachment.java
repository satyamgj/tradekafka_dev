package com.finmamba.models.communication.email;

import org.springframework.core.io.ByteArrayResource;

import lombok.Data;

@Data
public class EmailAttachment  {

    String attachmentName;

    byte[] attachmentData;

//    MediaType contentType() throws IOException{
//    	
//    }

    ByteArrayResource getInputStream() {
        return new ByteArrayResource(getAttachmentData());
    }

}
