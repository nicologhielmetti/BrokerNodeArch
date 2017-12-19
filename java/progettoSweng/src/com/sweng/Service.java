package com.sweng;

import com.jsonrpc.*;
import com.jsonrpc.Error;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            JsonRpcMessage receivedRpcRequest = null;
            try {
                receivedRpcRequest = this.manager.listenRequest();
            } catch (ParseException e) {
                System.err.println("Parse exeption");
                this.manager.send(JsonRpcDefaultError.parseError());
            }
            if (receivedRpcRequest.isBatch()) { //if is a batch request
                JsonRpcBatchRequest batch = (JsonRpcBatchRequest) receivedRpcRequest;
                List<JsonRpcRequest> requests = batch.get();
                List<JsonRpcResponse> responses = new ArrayList<>();
                for (Iterator<JsonRpcRequest> i = requests.iterator(); i.hasNext();) {
                    JsonRpcRequest request = i.next();
                    //if (!request.isEmpty()) {
                        JsonRpcResponse serviceResult = this.processRequest(request);
                        if (!request.isNotification()) // if is a notification no response is generated
                            responses.add(serviceResult);
                    //}
                }
                JsonRpcBatchResponse batchResponse = new JsonRpcBatchResponse();
                batchResponse.add(responses);
                this.manager.send(batchResponse);
            } else { // else if is a single JsonRpcRequest
                JsonRpcRequest request = (JsonRpcRequest) receivedRpcRequest;
                //if (!request.isEmpty()) {
                    //Execute request
                    JsonRpcResponse serviceResult = this.processRequest(request);
                    if (!request.isNotification()) // if is a notification no response return is generated
                        // Send response
                        this.manager.send(serviceResult);
                //}
            }
        }
    }

    private JsonRpcResponse processRequest(JsonRpcRequest request) {
        try {
            return this.function.run(request);
        } catch(RuntimeException e) {
            System.err.println("Runtime exeption in IServiceMethod implementation");
            return JsonRpcResponse.error(JsonRpcCustomError.internalServiceError(), request.getID());
        }
    }

    public ServiceMetadata getServiceMetadata() {
        return this.serviceMetadata;
    }

    public void delete() {
        this.serviceMetadata = null;
        this.manager = null;
    }
}
