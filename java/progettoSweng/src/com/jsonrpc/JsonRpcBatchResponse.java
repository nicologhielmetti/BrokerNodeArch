package com.jsonrpc;

import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;

public class JsonRpcBatchResponse extends JsonRpcMessage {
    private JSONArray jsonArray;

    protected JsonRpcBatchResponse(JSONArray jsonArray){
        this.jsonArray = jsonArray;
    }

    public JsonRpcBatchResponse() {
        jsonArray = new JSONArray();
    }

    public void addResponse(JsonRpcResponse response){
        jsonArray.add(response);
    }

    public void addResponses(ArrayList<JsonRpcResponse> responses){
        jsonArray.addAll(responses);
    }

    public void addResponseAt(JsonRpcResponse response, int index){
        jsonArray.add(index,response);
    }

    @Override
    public String toString() {
        return jsonArray.toJSONString();
    }

    public ArrayList<JsonRpcResponse> getResponses(){
        return new ArrayList<> (Arrays.asList((JsonRpcResponse[])jsonArray.toArray()));
    }
}
