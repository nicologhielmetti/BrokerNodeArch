package com.jsonrpc;

import com.google.gson.JsonObject;

public class Error {
    JsonObject jsonObject;

    public Error(int code, String message) {
        this.jsonObject = new JsonObject();
        jsonObject.addProperty("code",code);
        jsonObject.addProperty("message", message);
    }

    public Error(int code, String message, JsonObject data) {
        this(code, message);
        this.jsonObject.addProperty("data", data.toString());
    }

    public int getCode() {
        return this.jsonObject.get("code").getAsInt();
    }

    public String getMessage() {
        return this.jsonObject.get("message").getAsString();
    }

    public JsonObject getData() {
        return this.jsonObject.get("data").getAsJsonObject();
    }

    protected JsonObject getJsonObject(){
        return jsonObject;
    }

}
