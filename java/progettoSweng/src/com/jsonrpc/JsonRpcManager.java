package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonRpcManager {
    private IConnection connection;
    private JSONParser parser;
    private static String jsonRpcVersion = "2.0";

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
        JSONObject jsonObject;
        JSONArray jsonArray;
        do {
            Object obj=parser.parse(connection.read());
            jsonObject = (JSONObject) obj;
            if(jsonObject == null){
                jsonArray= (JSONArray) obj;
                if(jsonArray!=null){
                    //Batch
                }else throw new NullPointerException();
            }

            System.out.println("listenRequest(): jsonObject = "+jsonObject.toJSONString());
        } while(!(JsonRpcMessage.isRequest(jsonObject) || JsonRpcMessage.isError(jsonObject))); // ^ --> exclusive or

        connection.consume();
        return new JsonRpcRequest(jsonObject);
    }

    public JsonRpcResponse listenResponse() throws ParseException,NullPointerException {
        JSONObject jsonObject;
        do {
            jsonObject = (JSONObject) parser.parse(connection.read());
            if(jsonObject == null)
                throw new NullPointerException();
        }while (!(JsonRpcMessage.isResponse(jsonObject) ^ JsonRpcMessage.isError(jsonObject))); // ^ --> exclusive or
        connection.consume();
        return new JsonRpcResponse(jsonObject);
    }

    public void sendResponse(JsonRpcResponse response){ connection.send(response.toString()); }
    public void sendRequest(JsonRpcRequest request){ connection.send(request.toString()); }

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

    public void sendError(Error error, ID id){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc",jsonRpcVersion);
        jsonObject.put("error",error);
        jsonObject.put("id",id);
        sendResponse(new JsonRpcResponse(jsonObject));
    }

}
