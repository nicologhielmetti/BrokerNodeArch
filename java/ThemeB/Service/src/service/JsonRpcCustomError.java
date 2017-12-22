package service;

import jsonrpclibrary.Error;

/**
 * In this class are defined the JsonRpc application error.
 * From the JsonRpc specification http://www.jsonrpc.org/specification an application can
 * implement error code from -32000 to -32099
 */
public class JsonRpcCustomError {

    public static Error wrongSearchStrategy() {
        return new Error(-32000, "SearchStrategy is ill-formed");
    }

    public static Error localParseError() {
        return new Error(-32001, "Local parse error");
    }

    public static Error internalServiceError() {
        return new Error(-32002, "Internal service Error");
    }

    public static Error wrongParametersReceived() { return new Error(-32603, "Wrong parameters received"); }

    public static Error connectionTimeout() { return new Error(-32604, "Connection timeout"); }
}
