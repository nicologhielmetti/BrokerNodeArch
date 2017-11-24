package com.sweng;

import com.jsonrpc.JsonRpcRequest;
import com.jsonrpc.JsonRpcResponse;

import java.util.function.Function;

public class Service {

    private ServiceMetadata serviceMetadata;
    private Function function;

    public Service(Function function, ServiceMetadata serviceMetadata) {
        this.function = function;
        this.serviceMetadata = serviceMetadata;
    }

    public JsonRpcResponse processRequest(JsonRpcRequest request) {
        JsonRpcResponse response = null;
        // do something
        return response;
    }

}
