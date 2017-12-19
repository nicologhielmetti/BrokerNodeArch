package com.sweng;

import com.jsonrpc.Error;

/**
 * In this class are defined the JsonRpc application error.
 * From the JsonRpc specification http://www.jsonrpc.org/specification an application can
 * implemet error code from -32000 to -32099
 */
public class JsonRpcCustomError {

    public static Error wrongSerchStrategy() {
        return new Error(-32000, "SearchStrategy is ill-formed");
    }

    public static Error localParseError() {
        return new Error(-32001, "Local parse error");
    }

    public static Error internalServiceError() {
        return new Error(-32002, "Internal service Error");
    }

    public static Error wrongParametersReceived() {
        return new Error(-32603, "Wrong parameters received");
    }
}
