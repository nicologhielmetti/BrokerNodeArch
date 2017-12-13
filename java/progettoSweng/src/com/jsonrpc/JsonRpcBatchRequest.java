package com.jsonrpc;

import org.json.simple.JSONArray;

public class JsonRpcBatchRequest extends JsonRpcMessage {
    private JSONArray jsonArray;

    public JsonRpcBatchRequest(JSONArray jsonArray){
        this.jsonArray=jsonArray;
    }

}
