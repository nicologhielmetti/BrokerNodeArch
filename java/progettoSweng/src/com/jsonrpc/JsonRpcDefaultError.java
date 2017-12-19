package com.jsonrpc;

import com.google.gson.JsonPrimitive;
import com.jsonrpc.Error;

public class JsonRpcDefaultError {

    // There are no parameters because of parse error, can't access to ID field
    public static JsonRpcResponse parseError() {
        Error error = new Error(-32700, "Parse error");
        return JsonRpcResponse.error(error, null);
    }

    // There are no parameters because of invalid request, can't access to ID field
    public static JsonRpcResponse invalidRequest() {
        Error error = new Error(-32600, "Invalid Request");
        return JsonRpcResponse.error(error, null);
    }

    public static JsonRpcResponse methodNotFound(ID id) {
        Error error = new Error(-32601, "Method not found");
        return JsonRpcResponse.error(error, id);
    }

    public static JsonRpcResponse invalidRequest(ID id) {
        Error error = new Error(-32602, "Invalid params");
        return JsonRpcResponse.error(error, id);
    }

    public static JsonRpcResponse internalError(ID id) {
        Error error = new Error(-32603, "Internal error");
        return JsonRpcResponse.error(error,id);
    }

}
