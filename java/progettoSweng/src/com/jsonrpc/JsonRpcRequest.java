package com.jsonrpc;

import org.json.simple.JSONObject;

public class JsonRpcRequest extends JsonRpcMessage {

    protected JsonRpcRequest(JSONObject jsonObject) {
        json = jsonObject;
    }

    public JsonRpcRequest(String method,JSONObject params, int id){
        json.put("method",method);
        json.put("params",params);
        json.put("id",id);
    }

    public void setMethod(String method){
        json.put("method", method);
    }

    public void setParams(JSONObject params){
        json.put("params", params);
    }

    public JSONObject getParams(){
        return (JSONObject) json.get("params");
    }

    public String getMethod(){
        return (String) json.get("method");
    }

    public void setNotification(){
        if(json.containsKey("id"))
            json.remove("id");
    }

    public boolean isNotification(){
        return json.containsKey("id");
    }
}
