package com.sweng;

import com.jsonrpc.JsonRpcRequest;
import com.jsonrpc.JsonRpcResponse;
import org.json.simple.JSONObject;

public class Service {

    private ServiceMetadata serviceMetadata;
    private ServiceMethod function;

    public Service(ServiceMethod function, ServiceMetadata serviceMetadata) {
        this.function = function;
        this.serviceMetadata = serviceMetadata;
    }

    public JSONObject processRequest(JsonRpcRequest request) {
        JSONObject response = this.function.run(null);
        return response;
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }
}
