package com.jsonrpc;

import org.json.simple.JSONObject;

public class JsonRpcRequest extends JsonRpcMessage {

    public JsonRpcRequest(JSONObject jsonObject) {
        json = jsonObject;
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
