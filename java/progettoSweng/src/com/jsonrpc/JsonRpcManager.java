package com.jsonrpc;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonRpcManager {
    private IConnection connection;
    private JSONParser parser;

    private void getJsonRpcMessage(){
        try {
            Object o = parser.parse(connection.receive());
            JSONObject json = (JSONObject) o;
            if(json.containsKey("method"))

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public JsonRpcRequest getRequest(){
        return null;
    }

    public JsonRpcResponse getResponse(){
        return null;
    }

    public void sendResponse(JsonRpcResponse response){
        connection.send(response.toJsonRpcString());
    }
    public void sendRequest(JsonRpcRequest request){
        connection.send(request.toJsonRpcString());
    }

}
