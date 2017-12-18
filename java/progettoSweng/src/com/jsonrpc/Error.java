package com.jsonrpc;

import com.google.gson.JsonObject;

public class Error {
    int code;
    String message;
    JsonObject data = null;

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Error(int code, String message, JsonObject data) {
        this(code, message);
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public JsonObject getData() {
        return data;
    }
}
