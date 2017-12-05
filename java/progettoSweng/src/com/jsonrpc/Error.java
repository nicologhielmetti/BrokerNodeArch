package com.jsonrpc;

import org.json.simple.JSONObject;

public class Error extends JSONObject {
    public Error(String code, String message) {
        put("code",code);
        put("message",message);
    }

    public String getCode(){
        return (String) get("code");
    }

    public String getMessage(){
        return (String) get("message");
    }
}
