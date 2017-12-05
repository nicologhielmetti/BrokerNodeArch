package com.jsonrpc;

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

    public JsonRpcRequest getRequest(){
        JSONObject json;
        do {
            Object o = null;
            try {
                o = parser.parse(connection.read());
            } catch (ParseException e) {
                e.printStackTrace(); //string is not json
            }
            json = (JSONObject) o;
            if(json == null)
                connection.consume();
        } while(json == null || !(json.containsKey("method") && json.containsKey("id") && json.containsKey("jsonrpc")));
        connection.consume();
        return new JsonRpcRequest();
    }

    public JsonRpcResponse getResponse(){
        JSONObject json;
        do {
            Object o = null;
            try {
                o = parser.parse(connection.read());
            } catch (ParseException e) {
                e.printStackTrace(); //string is not json
            }
            json = (JSONObject) o;
            if(json == null)
                connection.consume();
        }while (json == null || !((json.containsKey("result") ^ json.containsKey("error")) && json.containsKey(""))); // ^ --> exclusive or
        connection.consume();
        if(json.containsKey("result")){ //if it is a response

        } else if(json.containsKey("error")){ //if it is an error

        }
        return new JsonRpcResponse();
    }

    public void sendResponse(JsonRpcResponse response){
        connection.send(response.toJsonRpcString());
    }
    public void sendRequest(JsonRpcRequest request){
        connection.send(request.toJsonRpcString());
    }

}
