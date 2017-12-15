package com.sweng;

import com.jsonrpc.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Service extends Thread {

    private ServiceMetadata serviceMetadata;
    private JsonRpcManager manager;
    private IServiceMethod function;

    public Service(ServiceMetadata serviceMetadata, IServiceMethod function) {
        this.serviceMetadata = serviceMetadata;
        this.function = function;
    }

    public void run() {
        while (true) {
            // Wait request
            JsonRpcMessage receivedRpcRequest = this.manager.listenRequest();
            if (receivedRpcRequest.isBatch()) { //if is a batch request
                JsonRpcBatchRequest batch = (JsonRpcBatchRequest) receivedRpcRequest;
                ArrayList<JsonRpcRequest> requests = batch.getRequests();
                ArrayList<JsonRpcResponse> responses = new ArrayList<>();
                for (Iterator<JsonRpcRequest> i = requests.iterator(); i.hasNext();) {
                    JsonRpcRequest request = i.next();
                    if (!request.isEmpty()) {
                        if (request.isNotification()) {
                            this.processRequest(request);
                        } else {
                            JsonRpcResponse serviceResult = this.processRequest(request);
                            responses.add(serviceResult);
                        }
                    }
                }
                JsonRpcBatchResponse batchResponse = new JsonRpcBatchResponse();
                batchResponse.addResponses(responses);
                this.manager.sendResponse(batchResponse);
            } else { // else if is a single JsonRpcRequest
                JsonRpcRequest request = (JsonRpcRequest) receivedRpcRequest;
                if (!request.isEmpty()) {
                    if (request.isNotification()) {
                        this.processRequest(request);
                    } else {
                        //Execute request
                        JsonRpcResponse serviceResult = this.processRequest(request);
                        // Send response
                        this.manager.sendResponse(serviceResult);
                    }
                }
            }
        }
    }

    private JsonRpcResponse processRequest(JsonRpcRequest request) { return this.function.run(request); }

    public ServiceMetadata getServiceMetadata() {
        return this.serviceMetadata;
    }

    public void delete() {
        this.serviceMetadata = null;
        this.manager = null;
    }
}
