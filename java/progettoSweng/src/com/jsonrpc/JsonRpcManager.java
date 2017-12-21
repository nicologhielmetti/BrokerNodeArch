package com.jsonrpc;

import com.google.gson.Gson;

public class JsonRpcManager {
    private com.jsonrpc.IConnection connection;

    public JsonRpcManager(com.jsonrpc.IConnection connection) {
        this.connection = connection;
    }

    public com.jsonrpc.JsonRpcMessage listenRequest() throws ParseException {
        com.jsonrpc.JsonRpcMessage msg;
        do {
            msg = listen();
        } while (!(msg instanceof com.jsonrpc.JsonRpcRequest) && !(msg instanceof com.jsonrpc.JsonRpcBatchRequest));
        connection.consume();
        return msg;
    }

    public com.jsonrpc.JsonRpcMessage listenResponse() throws ParseException {
        com.jsonrpc.JsonRpcMessage msg;
        do {
            msg = listen();
        } while (!(msg instanceof com.jsonrpc.JsonRpcResponse) && !(msg instanceof com.jsonrpc.JsonRpcBatchResponse));
        connection.consume();
        return msg;
    }


    private com.jsonrpc.JsonRpcMessage listen() throws ParseException {
        Gson gson = new Gson();
        String input = connection.read().trim();
        com.jsonrpc.JsonRpcMessage msg = null;

        if(input.charAt(0)=='['){
            msg = com.jsonrpc.JsonRpcBatchRequest.fromJson(input);
            if (msg != null) return msg;
            msg = com.jsonrpc.JsonRpcBatchResponse.fromJson(input);
            if (msg != null) return msg;
            //error

        }else {
            msg = com.jsonrpc.JsonRpcRequest.fromJson(input);
            if (msg != null) return msg;
            msg = com.jsonrpc.JsonRpcResponse.fromJson(input);
            if (msg != null) return msg;
        }


        connection.consume();
        throw new ParseException("\""+input+ "\" is not a valid json-rpc message");
    }


    public void send(com.jsonrpc.JsonRpcMessage msg) {
        connection.send(msg.toString());
    }
}
