package jsonrpclibrary;


public class JsonRpcDefaultError {

    public static Error parseError() {
        return new Error(-32700, "Parse error");
    }

    public static Error invalidRequest() {
        return new Error(-32600, "Invalid Request");
    }

    public static Error methodNotFound() {
        return new Error(-32601, "Method not found");
    }

    public static Error invalidParams() { return new Error(-32602, "Invalid params"); }

    public static Error internalError() {
        return new Error(-32603, "Internal error");
    }

}
