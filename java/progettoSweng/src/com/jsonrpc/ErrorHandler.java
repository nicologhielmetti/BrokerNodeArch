package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ErrorHandler {
    public JSONArray handleWrongBatchResponse(JSONArray jsonArray){
        for(JSONObject jsonObject : (JSONObject[]) jsonArray.toArray()){

        }
    }

    public Error handleWrongRequest(JSONObject jsonObject){

    }

    public Error handleWrongResponse(JSONObject jsonObject){

    }

    public JSONArray handleWrongBatchRequest(JSONArray jsonArray){
        for(JSONObject jsonObject : (JSONObject[]) jsonArray.toArray()){
            if(!JsonRpcMessage.isRequest(jsonObject))
        }
    }
}
