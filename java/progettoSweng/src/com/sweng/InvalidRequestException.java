package com.sweng;

public class InvalidRequestException extends Exception {

    public InvalidRequestException(String msg){
        super(msg);
    }
    public String getMessage()
    {
        return super.getMessage();
    }
}
