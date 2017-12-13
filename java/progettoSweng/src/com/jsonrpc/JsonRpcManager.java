package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class JsonRpcManager {
    private IConnection connection;
    private JSONParser parser;
    private static String jsonRpcVersion = "2.0";
    private ErrorHandler errorHandler;

    public static void setJsonRpcVersion(String jsonRpcVersion) {
        JsonRpcManager.jsonRpcVersion = jsonRpcVersion;
    }

    public static String getJsonRpcVersion() {
        return jsonRpcVersion;
    }

    public JsonRpcManager(IConnection connection) {
        this.connection = connection;
        this.parser = new JSONParser();
    }


    public JsonRpcRequest listenRequest() throws ParseException,NullPointerException {
        while(true) {
            Object obj = parser.parse(connection.read());
            if(obj == null)
                throw new NullPointerException();
            if(obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                if(JsonRpcMessage.isRequestBatch(jsonArray)) //handle batches
                    return new JsonRpcRequest(jsonArray);
                else
                    handleWrongBatch(jsonArray); //handle wrong messages
            } else if(obj instanceof JSONObject){
                //todo handle single Request
            } else {
                //todo handle non-json Request
            }
            if (JsonRpcMessage.isRequest(jsonObject) ^ JsonRpcMessage.isNotification(jsonObject)) {
                connection.consume();
                return new JsonRpcRequest(jsonObject);
            } else
                handleError(jsonObject);
        }
    }

    public JsonRpcResponse listenResponse() throws ParseException,NullPointerException {
        JSONObject jsonObject;
        do {
            jsonObject = (JSONObject) parser.parse(connection.read());
            if(jsonObject == null)
                throw new NullPointerException();
        }while (JsonRpcMessage.isResponse(jsonObject) == JsonRpcMessage.isNotification(jsonObject));
        connection.consume();
        return new JsonRpcResponse(jsonObject);
    }

    public void sendResponse(JsonRpcResponse response){ connection.send(response.toString()); }
    public void sendRequest(JsonRpcRequest request){ connection.send(request.toString()); }

    public void sendBatchResponse(ArrayList<JsonRpcResponse> responses){
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(responses);
        connection.send(jsonArray.toJSONString());
    }

    public void sendBatchRequest(ArrayList<JsonRpcRequest> requests){
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(requests);
        connection.send(jsonArray.toJSONString());
    }

    public void sendNotification(String method, JSONObject params){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc",jsonRpcVersion);
        jsonObject.put("method", method);
        jsonObject.put("params",params);
        sendRequest(new JsonRpcRequest(jsonObject));
    }

    public void sendNotification(String method){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc",jsonRpcVersion);
        jsonObject.put("method", method);
        sendRequest(new JsonRpcRequest(jsonObject));
    }

    public void sendError(Error error, int id){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc",jsonRpcVersion);
        jsonObject.put("error",error);
        jsonObject.put("id",id);
        sendResponse(new JsonRpcResponse(jsonObject));
    }

}
