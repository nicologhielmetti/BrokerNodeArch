package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonRpcResponse extends JsonRpcMessage {
    private JSONObject jsonObject;
    protected JsonRpcResponse(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    protected JsonRpcResponse(){} //empty response used for a particular case (batch == [])

    public void setJsonRpcVersion(String jsonRpc){
        jsonObject.put("jsonrpc",jsonRpc);
    }

    public String getJsonRpcVersion(){
        return (String) jsonObject.get("jsonrpc");
    }

    public void setId(ID id){
        jsonObject.put("id",id);
    }

    public ID getId(){
        return (ID) jsonObject.get("id");
    }

    public JsonRpcResponse(Error error, ID id) {
        jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", getJsonRpcVersion() );
        jsonObject.put("error",error);
        jsonObject.put("id", id);
    }

    public JsonRpcResponse(JSONObject result, ID id) {
        jsonObject.put("jsonrpc", getJsonRpcVersion() );
        jsonObject.put("result",result);
        jsonObject.put("id",id);
    }

    public JsonRpcResponse(JSONArray result, ID id){
        jsonObject.put("jsonrpc", getJsonRpcVersion() );
        jsonObject.put("result",result);
        jsonObject.put("id",id);
    }

    public void setError(Error error) {
        jsonObject.put("error",error);
    }

    public Error getError() {
        return (Error) jsonObject.get("error");
    }

    public boolean isError(){
        return(jsonObject.containsKey("error"));
    }

    public JSONObject getResult() {
        return (JSONObject) jsonObject.get("result");
    }

    public JSONObject getJsonRpc() {
        return jsonObject;
    }

    public void setResult(JSONObject result){
        jsonObject.put("result",result);
    }

    @Override
    public String toString() {
        return jsonObject.toJSONString();
    }
}
