package com.jsonrpc;

import org.json.simple.JSONObject;

public class JsonRpcResponse extends JsonRpcMessage {

    public JsonRpcResponse(String jsonrpc, JSONObject result, int id){
        json.put("jsonrpc",jsonrpc);
        json.put("result", result);
        json.put("id",id);
    }

    public JsonRpcResponse(String jsonrpc, Error error, JSONObject result, int id){
        json.put("jsonrpc",jsonrpc);
        json.put("error",error);
        json.put("result", result);
        json.put("id",id);
    }

    public void setError(Error error) {
        json.put("error",error);
    }

    public Error getError() {
        return (Error) json.get("error");
    }

    public boolean isError(){
        return(json.containsKey("error"));
    }

    public JSONObject getResult() {
        return (JSONObject) json.get("result");
    }

}
