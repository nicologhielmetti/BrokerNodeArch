package com.jsonrpc;

import com.google.gson.Gson;

public class JsonRpcManager {
    private IConnection connection;

    public JsonRpcManager(IConnection connection) {
        this.connection = connection;
    }

    public JsonRpcMessage listenRequest() throws ParseException {
        JsonRpcMessage msg = null;
        do {

            try {
                msg = listen(-1);
            } catch (TimeoutException e) {
                e.printStackTrace(); //should never happen
            }
        } while (!(msg instanceof JsonRpcRequest) && !(msg instanceof JsonRpcBatchRequest));
        connection.consume();
        return msg;
    }

    public JsonRpcMessage listenRequest(long milliseconds) throws ParseException, TimeoutException {
        JsonRpcMessage msg;
        do {
            msg = listen(milliseconds);
        } while (!(msg instanceof JsonRpcRequest) && !(msg instanceof JsonRpcBatchRequest));
        connection.consume();
        return msg;
    }

    public JsonRpcMessage listenResponse() throws ParseException {
        JsonRpcMessage msg = null;
        do {
            try {
                msg = listen(-1);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        } while (!(msg instanceof JsonRpcResponse) && !(msg instanceof JsonRpcBatchResponse));
        connection.consume();
        return msg;
    }

    public JsonRpcMessage listenResponse(long milliseconds) throws ParseException, TimeoutException {
        JsonRpcMessage msg = null;
        do {
            msg = listen(milliseconds);
        } while (!(msg instanceof JsonRpcResponse) && !(msg instanceof JsonRpcBatchResponse));
        connection.consume();
        return msg;
    }

    /**
     * @param milliseconds: If nothing arrive in that time a TimeoutException is thrown.
     *                      If it is a negative number it waits forever.
     * @return
     * @throws ParseException
     * @throws TimeoutException
     */
    private JsonRpcMessage listen(long milliseconds) throws ParseException, TimeoutException {
        Gson gson = new Gson();
        String input = milliseconds >= 0 ? connection.read(milliseconds) : connection.read();
        if (input == null) throw new TimeoutException("");

        input = input.trim();
        JsonRpcMessage msg = null;

        if (input.charAt(0) == '[') {
            msg = JsonRpcBatchRequest.fromJson(input);
            if (msg != null) return msg;
            msg = JsonRpcBatchResponse.fromJson(input);
            if (msg != null) return msg;
            //error
            connection.consume();
            throw new ParseException("\"" + input + "\" is not a valid json-rpc batch");
        } else {
            msg = JsonRpcRequest.fromJson(input);
            if (msg != null) return msg;
            msg = JsonRpcResponse.fromJson(input);
            if (msg != null) return msg;
            connection.consume();
            throw new ParseException("\"" + input + "\" is not a valid json-rpc message");
        }
    }

    public IConnection getConnection(){
        return connection;
    }
    public void send(JsonRpcMessage msg) {
        connection.send(msg.toString());
    }
}
