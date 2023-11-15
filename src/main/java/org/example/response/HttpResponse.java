package org.example.response;

import org.example.enums.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

public class HttpResponse {

    private final Object responseObject;
    private final HttpStatus status;

    public HttpResponse(Object responseObject, HttpStatus status) {
        this.responseObject = responseObject;
        this.status = status;
    }

    public Object getResponseObject() {
        return responseObject;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
