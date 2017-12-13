package com.jsonrpc;

public class JsonRpcDefaultError {

    // There are no parameters because of parse error, can't access to ID field
    public static JsonRpcResponse parseError() {
        Error error = new Error("-32700", "Parse error");
        JsonRpcResponse response = new JsonRpcResponse(error, null);
        return response;
    }

    // There are no parameters because of invalid request, can't access to ID field
    public static JsonRpcResponse InvalidRequest() {
        Error error = new Error("-32600", "Invalid Request");
        JsonRpcResponse response = new JsonRpcResponse(error, null);
        return response;
    }

    public static JsonRpcResponse MethodNotFound(ID id) {
        Error error = new Error("-32601", "Method not found");
        JsonRpcResponse response = new JsonRpcResponse(error, id);
        return response;
    }

    public static JsonRpcResponse InvalidRequest(ID id) {
        Error error = new Error("-32602", "Invalid params");
        JsonRpcResponse response = new JsonRpcResponse(error, id);
        return response;
    }

    public static JsonRpcResponse InternalError(ID id) {
        Error error = new Error("-32603", "Internal error");
        JsonRpcResponse response = new JsonRpcResponse(error, id);
        return response;
    }


}
