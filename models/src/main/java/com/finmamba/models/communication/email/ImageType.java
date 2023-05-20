package com.finmamba.models.communication.email;

import org.springframework.lang.NonNull;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ImageType {
    GIF("gif", "image/gif"),
    JPG("jpg", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"),
    PNG("png", "image/png");

    private final String extension;
    private final String contentType;

    ImageType(@NonNull final String extension, @NonNull final String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }
}
