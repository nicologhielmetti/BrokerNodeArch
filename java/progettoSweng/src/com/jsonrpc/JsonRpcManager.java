package com.jsonrpc;

import com.google.gson.Gson;
import com.jsonrpc.ParseException;

public class JsonRpcManager {
    private IConnection connection;

    public JsonRpcManager(IConnection connection) {
        this.connection = connection;
    }

    public JsonRpcMessage listenRequest() throws ParseException {
        JsonRpcMessage msg;
        do {
            msg = listen();
        } while (!(msg instanceof JsonRpcRequest) && !(msg instanceof JsonRpcBatchRequest));
        connection.consume();
        return msg;
    }

    public JsonRpcMessage listenResponse() throws ParseException {
        JsonRpcMessage msg;
        do {
            msg = listen();
        } while (!(msg instanceof JsonRpcResponse) && !(msg instanceof JsonRpcBatchResponse));
        connection.consume();
        return msg;
    }


    private JsonRpcMessage listen() throws ParseException {
        Gson gson = new Gson();
        String input = connection.read();
        JsonRpcMessage msg = JsonRpcRequest.fromJson(input);
        if (msg != null) return msg;
        msg = JsonRpcResponse.fromJson(input);
        if (msg != null) return msg;
        msg = JsonRpcBatchRequest.fromJson(input);
        if (msg != null) return msg;
        msg = JsonRpcBatchResponse.fromJson(input);
        if (msg != null) return msg;

        connection.consume();
        throw new ParseException("\""+input+ "\" is not a valid json-rpc message");
    }


    public void send(JsonRpcMessage msg) {
        connection.send(msg.toString());
    }
}
