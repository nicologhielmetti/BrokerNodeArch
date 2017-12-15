package com.sweng;

import com.jsonrpc.JsonRpcResponse;
import com.jsonrpc.JsonRpcRequest;

public interface IServiceMethod {

    JsonRpcResponse run(JsonRpcRequest parameters) throws RuntimeException;

}
