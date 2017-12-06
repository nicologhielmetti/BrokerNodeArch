package com.jsonrpc;

import org.json.simple.JSONObject;

public class JsonRpcResponse extends JsonRpcMessage {

    protected JsonRpcResponse(JSONObject jsonObject){
        this.json = jsonObject;
    }

    public JsonRpcResponse(Error error, int id) {
        json.put("error",error);
        json.put("id", id);
    }

    public JsonRpcResponse(JSONObject result, int id) {
        json.put("result",result);
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

    public void setResult(JSONObject result){
        json.put("result",result);
    }
}
