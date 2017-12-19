package com.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonRpcResponse extends JsonRpcMessage {

    private JsonObject json;

    private JsonRpcResponse(JsonObject json) {
        this.json = json;
    }

    private JsonRpcResponse(ID id) {
        json = new JsonObject();
        json.addProperty("jsonrpc", "2.0");
        if (id != null && !id.isNull()) {
            if (id.isString()) json.addProperty("id", id.getAsString());
            else json.addProperty("id", id.getAsInt());
        }
    }

    public JsonRpcResponse(JsonElement result, ID id) {
        this(id);
        json.add("result", result);
    }

    private JsonRpcResponse(Error e, ID id) {
        this(id);
        json.addProperty("error", (new Gson()).toJson(e));
    }


    public static JsonRpcResponse error(Error e, ID id) {
        return new JsonRpcResponse(e, id);
    }

    public ID getID() {
        JsonPrimitive j = json.getAsJsonPrimitive("id");
        if (j == null) return null;
        if (j.isString()) return new ID(j.getAsString());
        if (j.isNumber()) return new ID(j.getAsInt());
        if (j.isJsonNull()) return new ID();
        return null; //invalid id
    }

    public boolean isError() {
        return json.has("error");
    }

    public Error getError() {
        if (!isError()) return null;
        return (new Gson()).fromJson(json.get("error"), Error.class);
    }

    public JsonElement getResult() {
        return json.get("result");
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
            Error e = (new Gson()).fromJson(json.get("error"), Error.class);  //check if error is valid
            if (e == null) return null;
        }
        if (3 != json.size()) return null; //there are other fields -> is not a well-formed Json-RPC Request
        return new JsonRpcResponse(json);
    }
}