package com.sweng;

import com.jsonrpc.JsonRpcResponse;
import com.jsonrpc.JsonRpcRequest;

/**
 * This interface is used to define the service function.
 * This function is running during the all service lifetime.
 */

public interface IServiceMethod {

    JsonRpcResponse run(JsonRpcRequest parameters) throws RuntimeException;

}
