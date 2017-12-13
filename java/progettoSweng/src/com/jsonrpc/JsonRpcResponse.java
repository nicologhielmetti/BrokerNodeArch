package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonRpcResponse extends JsonRpcMessage {
    private JSONObject jsonObject;
    protected JsonRpcResponse(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public void setJsonRpcVersion(String jsonRpc){
        jsonObject.put("jsonrpc",jsonRpc);
    }

    public String getJsonRpcVersion(){
        return (String) jsonObject.get("jsonrpc");
    }

    public void setId(ID id){
        if(id==null)jsonObject.put("id",null);
        else if(id.isString())this.jsonObject.put("id",id.toString());
        else if(id.isInt())this.jsonObject.put("id",((Integer)id.getId()).intValue());
    }

    public ID getId(){
        return new ID(jsonObject.get("id"));
    }

    public JsonRpcResponse(Error error, ID id) {
        jsonObject=new JSONObject();
        jsonObject.put("error",error);
        jsonObject.put("id", id);
    }

    public JsonRpcResponse(JSONObject result, ID id) {
        jsonObject=new JSONObject();
        jsonObject.put("result",result);
        if(id==null)jsonObject.put("id",null);
        else if(id.isString())this.jsonObject.put("id",id.toString());
        else if(id.isInt())this.jsonObject.put("id",((Integer)id.getId()).intValue());
    }

    public JsonRpcResponse(JSONArray result, ID id){
        jsonObject=new JSONObject();
        jsonObject.put("result",result);
        if(id==null)jsonObject.put("id",null);
        else if(id.isString())this.jsonObject.put("id",id.toString());
        else if(id.isInt())this.jsonObject.put("id",((Integer)id.getId()).intValue());
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
}
