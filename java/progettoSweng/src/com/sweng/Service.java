package com.sweng;

import com.jsonrpc.JsonRpcRequest;
import org.json.simple.JSONObject;

public class Service {

    private ServiceMetadata serviceMetadata;
    private IServiceMethod function;

    public Service(IServiceMethod function, ServiceMetadata serviceMetadata) {
        this.function = function;
        this.serviceMetadata = serviceMetadata;
    }

    public JSONObject processRequest(JSONObject request) {
        JSONObject result = this.function.run(null);
        return result;
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }
}
