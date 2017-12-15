package com.jsonrpc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonRpcManager {
    private IConnection connection;
    private JSONParser parser;
    private JsonRpcBatchResponse batchResponse;
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

    public JsonRpcManager() {
        this.parser = new JSONParser();
        this.batchResponse = new JsonRpcBatchResponse();
    }

    public JsonRpcMessage listenRequest() {
        Object obj;
        try {
            obj = parser.parse(connection.read());
        } catch (ParseException e) {
            connection.consume();
            System.err.println("Send parseError to client");
            sendResponse(JsonRpcDefaultError.parseError()); //parse error
            System.out.println("Gestite ->" + batchResponse.toString());
            return new JsonRpcRequest();
        }
        if(obj instanceof JSONArray) { //it could be a batchRequest
            JSONArray batchRequestReceived = (JSONArray) obj;
            connection.consume();
            if(JsonRpcMessage.isRequestBatch(batchRequestReceived)) { //true when ALL requests/notifications are correct
                System.out.println("Gestite ->" + batchResponse.toString());
                return new JsonRpcBatchRequest(batchRequestReceived);
            } else {
                JsonRpcMessage batchResponseToProvide = handleWrongBatchRequest(batchRequestReceived);
                if(batchResponseToProvide instanceof JsonRpcResponse) { //case []
                    sendResponse(JsonRpcDefaultError.InvalidRequest());
                    System.out.println("Gestite ->" + batchResponse.toString());
                    return new JsonRpcRequest();
                }
                System.out.println("Gestite ->" + batchResponse.toString());
                return batchResponseToProvide;
            }
        } else { //it could be a single request/notification
            JSONObject requestReceived = (JSONObject) obj;
            connection.consume();
            if(JsonRpcMessage.isRequest(requestReceived) || JsonRpcMessage.isNotification(requestReceived)) { //true if it satisfy jsonrpc standard
                System.out.println("Gestite ->" + batchResponse.toString());
                return new JsonRpcRequest(requestReceived);
            } else {
                sendResponse(JsonRpcDefaultError.InvalidRequest());
                System.out.println("Gestite ->" + batchResponse.toString());
                return JsonRpcDefaultError.InvalidRequest();
            }
        }
    }


    public JsonRpcMessage listenResponse() {
        Object obj = null;
        try {
            obj = parser.parse(connection.read());
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Internal error");
            return JsonRpcDefaultError.InternalError();
        }
        if(obj instanceof JSONArray)                            //it could be a batchResponse
            return new JsonRpcBatchRequest((JSONArray) obj);
        else                                                    //it could be a single response/error
            return new JsonRpcRequest((JSONObject) obj);
    }

    private JsonRpcMessage handleWrongBatchRequest(JSONArray jsonArray){ //return the JsonRpcMessage composed by correct requests/notifications. Pass it to the upper layer.
        if(jsonArray.size() == 0)
            return new JsonRpcResponse();
        JSONArray correctRequests = new JSONArray();
        for(int i = 0; i < jsonArray.size(); i++) {
            try {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                if (!(JsonRpcMessage.isRequest(jsonObject) || JsonRpcMessage.isNotification(jsonObject)))
                    batchResponse.addResponse(JsonRpcDefaultError.InvalidRequest());
                else
                    correctRequests.add(jsonObject);
            } catch (ClassCastException e){
                batchResponse.addResponse(JsonRpcDefaultError.InvalidRequest());
            }
        }
        return new JsonRpcBatchRequest(correctRequests);
    }

    public void sendResponse(JsonRpcResponse response){ connection.send(response.toString()); }
    public void sendResponse(JsonRpcBatchResponse batchResponse){ connection.send(batchResponse.toString()); }
    public void sendRequest(JsonRpcBatchRequest batchRequest){ connection.send(batchRequest.toString()); }
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

    public void sendError(Error error, int id){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc",jsonRpcVersion);
        jsonObject.put("error",error);
        jsonObject.put("id",id);
        sendResponse(new JsonRpcResponse(jsonObject));
    }

    public void sendDefaultError(JsonRpcResponse defaultError){
        sendResponse(defaultError);
    }

}
