package service;

import jsonrpclibrary.JsonRpcRequest;
import jsonrpclibrary.JsonRpcResponse;

/**
 * This interface is used to define the service function.
 * This function is running during the all service lifetime.
 */

public interface IServiceMethod {

    JsonRpcResponse run(JsonRpcRequest parameters) throws RuntimeException;

}
