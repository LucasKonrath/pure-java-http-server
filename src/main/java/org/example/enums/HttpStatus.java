package org.example.enums;

public enum HttpStatus {
    OK(200),
    CREATED(201),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    private int code;

    HttpStatus(int code){
        this.code = code;
    }
    ;
    public int getCode(){
        return this.code;
    }
}
