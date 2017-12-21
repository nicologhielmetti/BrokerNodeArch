package com.jsonrpc;

import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonRpcResponse extends com.jsonrpc.JsonRpcMessage {

    private JsonObject json;

    private JsonRpcResponse(JsonObject json) {
        this.json = json;
    }

    private JsonRpcResponse(com.jsonrpc.ID id) {
        json = new JsonObject();
        json.addProperty("jsonrpc", "2.0");
        if (id != null && !id.isNull()) {
            if (id.isString()) json.addProperty("id", id.getAsString());
            else json.addProperty("id", id.getAsInt());
        }else json.add("id", JsonNull.INSTANCE);
    }

    public JsonRpcResponse(JsonElement result, com.jsonrpc.ID id) {
        this(id);
        json.add("result", result);
    }

    private JsonRpcResponse(com.jsonrpc.Error e, com.jsonrpc.ID id) {
        this(id);
        json.add("error", e.getJsonObject());
    }


    public static JsonRpcResponse error(com.jsonrpc.Error e, com.jsonrpc.ID id) {
        return new JsonRpcResponse(e, id);
    }

    public com.jsonrpc.ID getID() {
        JsonPrimitive j = json.getAsJsonPrimitive("id");
        if (j == null) return null;
        if (j.isString()) return new com.jsonrpc.ID(j.getAsString());
        if (j.isNumber()) return new com.jsonrpc.ID(j.getAsInt());
        if (j.isJsonNull()) return new com.jsonrpc.ID();
        return null; //invalid id
    }

    public boolean isError() {
        return json.has("error");
    }

    public com.jsonrpc.Error getError() {
        if (!isError()) return null;
        return (new Gson()).fromJson(json.get("error"), com.jsonrpc.Error.class);
    }

    public JsonElement getResult() {
        return json.has("result")?json.get("result"):null;
    }

    public String toString() {
        return toJson();
    }

    public String toJson() {
        return json.toString();
    }

    public static JsonRpcResponse fromJson(String str) {
        JsonObject json = (new Gson()).fromJson(str, JsonObject.class);
        if (json == null || !json.get("jsonrpc").getAsString().equals("2.0") || !json.has("id"))
            return null; // jsonrpc and id MUST be included
        if (json.has("result") == json.has("error"))
            return null; //"Either the result member or error member MUST be included, but both members MUST NOT be included."
        if (json.has("error")) {
            com.jsonrpc.Error e = (new Gson()).fromJson(json.get("error"), com.jsonrpc.Error.class);  //check if error is valid
            if (e == null) return null;
        }
        if (3 != json.size()) return null; //there are other fields -> is not a well-formed Json-RPC Request
        return new JsonRpcResponse(json);
    }
}