package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ErrorHandler {
    public JSONArray handleWrongBatchResponse(JSONArray jsonArray){
        for(JSONObject jsonObject : (JSONObject[]) jsonArray.toArray()){

        }
        return null;
    }

    public Error handleWrongRequest(JSONObject jsonObject){
        return null;
    }

    public Error handleWrongResponse(JSONObject jsonObject){
        return null;
    }

    public JSONArray handleWrongBatchRequest(JSONArray jsonArray){
        for(JSONObject jsonObject : (JSONObject[]) jsonArray.toArray()){
            //if(!JsonRpcMessage.isRequest(jsonObject))
        }
        return null;
    }
}
