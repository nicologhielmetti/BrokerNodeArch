package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class JsonRpcMessage {

    protected static JSONParser parser;

    public static boolean isResponseBatch(JSONArray jsonArray){
        JsonRpcResponse[] responses = (JsonRpcResponse[]) jsonArray.toArray();
        for(JsonRpcResponse r : responses)
            if(!(isResponse(r.getJsonRpc()) || isError(r.getJsonRpc())))
                return false;
        return true;
    }

    public boolean isBatch(){
        return ((this instanceof JsonRpcBatchResponse) || (this instanceof JsonRpcBatchRequest));
    }

    public static boolean isRequestBatch(JSONArray jsonArray){
        if(jsonArray.size() == 0) return false;
        try {
            JSONObject[] requests = (JSONObject[]) jsonArray.toArray(new JSONObject[jsonArray.size()]);
            for(JSONObject r : requests)
                if(isRequest(r) == isNotification(r))
                    return false;
            return true;
        } catch (ArrayStoreException e){
            return false;
        }
    }

    public static boolean isResponse(JSONObject jsonObject){
        return ((jsonObject.containsKey("jsonrpc") &&
                jsonObject.containsKey("result") &&
                jsonObject.containsKey("id")) && jsonObject.size() == 3);
    }

    public static boolean isError(JSONObject jsonObject){
        return ((jsonObject.containsKey("jsonrpc") &&
                jsonObject.containsKey("error") &&
                jsonObject.containsKey("id")) && jsonObject.size() == 3);
    }

    public static boolean isRequest(JSONObject jsonObject){
        return (((jsonObject.containsKey("jsonrpc") &&
                jsonObject.containsKey("method")  &&
                jsonObject.containsKey("id")) && jsonObject.size() == 3)
        ||
                ((jsonObject.containsKey("jsonrpc") &&
                jsonObject.containsKey("method")  &&
                jsonObject.containsKey("id")  &&
                jsonObject.containsKey("params")) && jsonObject.size() == 4)
        );
    }

    public static boolean isNotification(JSONObject jsonObject){
        return (jsonObject.containsKey("jsonrpc") &&
                jsonObject.containsKey("method")  &&
                !jsonObject.containsKey("id"));
    }

    public static boolean isJsonRpc(JSONObject jsonObject){
        return (isResponse(jsonObject) || isError(jsonObject)) ^ (isRequest(jsonObject) ^ isNotification(jsonObject));
    }

    public static JsonRpcMessage toJsonRpcObject(String jsonString) throws ParseException, InvalidJsonRpcException {
        JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
        if(isRequest(jsonObject) ^ isNotification(jsonObject))
            return new JsonRpcRequest(jsonObject);
        else if (isResponse(jsonObject)|| isError(jsonObject))
            return new JsonRpcResponse(jsonObject);
        throw new InvalidJsonRpcException();
    }


    public static JsonRpcResponse createJsonRpcResponse(JSONObject jsonObject){
        if(isResponse(jsonObject))
            return new JsonRpcResponse(jsonObject);
        else
            return null;
    }

}
