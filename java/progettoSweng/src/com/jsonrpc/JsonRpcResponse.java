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

    private JsonRpcResponse(JsonPrimitive id) {
        json = new JsonObject();
        json.addProperty("jsonrpc", "2.0");
        json.add("id", id);
    }

    public JsonRpcResponse(JsonElement result, JsonPrimitive id) {
        this(id);
        json.add("result", result);
    }

    public JsonRpcResponse(Error e, JsonPrimitive id) {
        this(id);
        json.addProperty("error", (new Gson()).toJson(e));
    }

    public JsonRpcResponse(JsonElement result, int id) {
        this(result, new JsonPrimitive(id));
    }

    public JsonRpcResponse(JsonElement result, String id) {
        this(result, new JsonPrimitive(id));
    }

    public static JsonRpcResponse error(Error e, JsonPrimitive id) {
        return new JsonRpcResponse(e, id);
    }

    public JsonPrimitive getID() {
        return json.getAsJsonPrimitive("id");
    }

    public boolean isError() {
        return json.has("error");
    }

    public Error getError() {
        if (!isError()) return null;
        return (new Gson()).fromJson(json.get("error"), Error.class);
    }

    public JsonObject getResult() {
        return json.get("result").getAsJsonObject();
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