package com.jsonrpc;

import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;

public class JsonRpcBatchRequest extends JsonRpcMessage {
    private JSONArray jsonArray;

    protected JsonRpcBatchRequest(JSONArray jsonArray){
        this.jsonArray = jsonArray;
    }

    public void addRequest(JsonRpcRequest request){
        jsonArray.add(request);
    }

    public void addRequests(ArrayList<JsonRpcRequest> requests){
        jsonArray.addAll(requests);
    }

    @Override
    public String toString() {
        return jsonArray.toJSONString();
    }

    public ArrayList<JsonRpcRequest> getRequests(){
        return new ArrayList<> (Arrays.asList((JsonRpcRequest[])jsonArray.toArray()));
    }
}
