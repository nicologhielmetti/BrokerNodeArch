package com.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;



public class JsonRpcRequest extends JsonRpcMessage {

    private JsonObject json = null;

    private JsonRpcRequest(JsonObject json) {
        this.json = json;
    }

    private JsonRpcRequest(String method, JsonElement params) {
        json = new JsonObject();
        json.addProperty("jsonrpc", "2.0");
        json.addProperty("method", method);
        if (params != null) json.add("params", params);
    }

    public JsonRpcRequest(String method, JsonElement params, ID id) {
        this(method, params);
        if(id!=null && !id.isNull()){
            if(id.isString())json.addProperty("id", id.getAsString());
            else json.addProperty("id",id.getAsInt());
        }
    }


    public static JsonRpcRequest notification(String method, JsonElement params) {
        JsonRpcRequest r = new JsonRpcRequest(method, params);
        return r;
    }



    public ID getID() {
        JsonPrimitive j=json.getAsJsonPrimitive("id");
        if(j==null)return null;
        if(j.isString())return new ID(j.getAsString());
        if(j.isNumber())return new ID(j.getAsInt());
        if(j.isJsonNull())return new ID();
        return null; //invalid id
    }

    public JsonElement getParams() {
        return json.get("params");
    }
    public String getMethod() {
        return json.get("method").getAsString();
    }

    public boolean isNotification() {
        return !json.has("id");
    }

    public static JsonRpcRequest invalid() {
        return new JsonRpcRequest(null);
    }
    public boolean isValid(){ return json!=null;}



    public String toString() {
        return toJson();
    }

    public String toJson() {
        return json.toString();
    }

    public static JsonRpcRequest fromJson(String str) {
        JsonObject json = (new Gson()).fromJson(str, JsonObject.class);
        if (json == null) return null;
        if (json.get("jsonrpc") == null || !json.get("jsonrpc").getAsString().equals("2.0") || !json.has("method"))
            return null; // jsonrpc and method MUST be included
        int fields = 0;
        if (json.has("id")) fields++;     //params MAY be omitted -> notification
        if (json.has("params")) fields++; //params MAY be omitted
        if (fields + 2 != json.size()) return null; //there are other fields -> is not a well-formed Json-RPC Request
        return new JsonRpcRequest(json);
    }


}
