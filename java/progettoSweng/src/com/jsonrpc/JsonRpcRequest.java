package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonRpcRequest extends JsonRpcMessage {

    JSONObject jsonObject;
    ID id;

    public void setJsonRpcVersion(String jsonRpc){
        jsonObject.put("jsonrpc",jsonRpc);
    }

    public String getJsonRpcVersion(){
        return (String) jsonObject.get("jsonrpc");
    }

    public void setId(ID id){
        this.id=id;
        if(id==null)jsonObject.put("id",null);
        else if(id.isString())this.jsonObject.put("id",id.toString());
        else if(id.isInt())this.jsonObject.put("id",((Integer)id.getId()).intValue());
    }

    public ID getId(){
        return id;//new ID(jsonObject.get("id"));
    }

    protected JsonRpcRequest(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }



    public JSONObject getJsonRpc() {
        return jsonObject;
    }

    @Override
    public String toString(){
        return jsonObject.toJSONString();
    }

    public JsonRpcRequest(String method, JSONObject params, ID id){
        this.id=id;

        this.jsonObject=new JSONObject();
        this.jsonObject.put("method",method);
        this.jsonObject.put("params",params);
        if(id==null)jsonObject.put("id",null);
        else if(id.isString())this.jsonObject.put("id",id.toString());
        else if(id.isInt())this.jsonObject.put("id",((Integer)id.getId()).intValue());

        this.jsonObject.put("jsonrpc","2.0");
    }

    public JsonRpcRequest() {
        jsonObject = new JSONObject();
    }

    public boolean isEmpty(){
        return (jsonObject.isEmpty());
    }

    public void setMethod(String method){
        this.jsonObject.put("method", method);
    }

    public void setParams(JSONObject params){
        this.jsonObject.put("params", params);
    }

    public JSONObject getParams(){
        return (JSONObject) this.jsonObject.get("params");
    }

    public String getMethod(){
        return (String) this.jsonObject.get("method");
    }

    public void setNotification(){
        if(this.jsonObject.containsKey("id"))
            this.jsonObject.remove("id");
    }

    public boolean isNotification(){
        return !this.jsonObject.containsKey("id");
    }
}
