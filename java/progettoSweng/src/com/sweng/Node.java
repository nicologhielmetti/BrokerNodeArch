package com.sweng;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.*;

import com.jsonrpc.Error;


import com.jsonrpc.*;
import javafx.util.Pair;

// todo timer on response

public class Node {

    private Map<String, Service> ownServices;
    private IConnectionFactory connectionFactory;
    private int id;

    // Start of Service handler functionality

    /**
     * Dictionary:
     * - Server ---> DummyServer.java
     * - Client ---> DummyClient.java
     */

    /**
     * Node is the constructor of the Node class.
     * The IConncetionFactory parameter is used to define which is used to define
     * the connection that this object have to use.
     * @param connectionFactory
     */

    public Node(IConnectionFactory connectionFactory) {
        this.id = 0;
        this.connectionFactory = connectionFactory;
        ownServices = new HashMap<>();
    }

    /**
     * provideService is public method that allow the application layer (servers) to provide
     * an own service to all clients connected in the system. With this method the server send
     * to the broker a request of adding service ("registerService") and the broker if the operation
     * is successful a response is return with the correct method name in order not to have duplicated
     * method identifier.
     * When the service is correctly published this method start a thread that wait requests from clients.
     * This
     * @param metadata, function
     */

    public boolean provideService(ServiceMetadata metadata, IServiceMethod function) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        Service service = new Service(metadata, function, manager);

        JsonRpcRequest registerServiceRequest = new JsonRpcRequest("registerService", metadata.toJson(), this.generateNewId());
        manager.send(registerServiceRequest);
        JsonRpcResponse registerServiceResponse = null;
        try {
            registerServiceResponse = (JsonRpcResponse) manager.listenResponse();
        } catch (com.jsonrpc.ParseException e) {
            e.printStackTrace();
        }

        // Read response from Broker
        JsonObject result = registerServiceResponse.getResult().getAsJsonObject();
        boolean serviceRegistered = result.get("serviceRegistered").getAsBoolean();
        if (serviceRegistered) {
            String newMethodName = result.get("method").getAsString();
            metadata.setMethodName(newMethodName);
        } else {
            // Timeout
        }
        System.out.println("Server: Service registered!");
        // Start new service
        service.start();
        ownServices.put(metadata.getMethodName(), service);
        return true;
    }

    public void deleteService(String method) {
        if (this.ownServices.containsKey(method)) {
            IConnection connection = this.connectionFactory.createConnection();
            JsonRpcManager manager = new JsonRpcManager(connection);

            JsonObject jsonMethod = new JsonObject();
            jsonMethod.addProperty("method", method);

            JsonRpcRequest request = JsonRpcRequest.notification("deleteService", jsonMethod);
            manager.send(request);
            // Delete service
            Service availableService = this.ownServices.get(method);
            availableService.interrupt();
            availableService.delete();
            this.ownServices.remove(method);
        } else {
            System.err.println("Server: There is no service named " + method);
        }
    }

    public void setConncetionFactory(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // End of Service handler functionality

    // Begin of Service requester functionality


    public JsonRpcResponse requestService(String method, JsonElement parameters) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        JsonRpcRequest request = new JsonRpcRequest(method, parameters, generateNewId());
        manager.send(request);
        JsonRpcResponse response = null;
        try {
            response = (JsonRpcResponse) manager.listenResponse();
        } catch (com.jsonrpc.ParseException e) {
            System.err.println("Client: Local parse exeption: " + response.toString());
            response = JsonRpcResponse.error(JsonRpcCustomError.localParseError(), ID.Null());
        }
        return response;
    }

    public JsonRpcMessage requestService(List<JsonRpcRequest> requests) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        JsonRpcBatchRequest batchRequest = new JsonRpcBatchRequest();
        batchRequest.add(requests);
        manager.send(batchRequest);
        JsonRpcMessage response;
        try {
            response = manager.listenResponse();
        } catch (com.jsonrpc.ParseException e) {
            System.err.println("Client: Local parse exeption: " + e.getCause().toString());
            response = JsonRpcResponse.error(JsonRpcCustomError.localParseError(), ID.Null());
        }
        return response;
    }

   public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<>();
        list.clear();
        JsonRpcResponse response = this.requestService("getServicesList", searchStrategy.toJsonElement());
        JsonArray array = response.getResult().getAsJsonArray();
        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext()) {
            list.add(ServiceMetadata.fromJson(iterator.next().getAsJsonObject()));
        }
        return list;
    }

    // End of Service requester functionality

    private ID generateNewId() {
        return new ID(this.id++);
    }

    public void showRunningServices() {
        Iterator<Map.Entry<String, Service>> i = ownServices.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String,Service> it = i.next();
            System.out.println("Name: " + it.getKey() + " - " + it.getValue().getServiceMetadata().toJson());
        }
    }


}
