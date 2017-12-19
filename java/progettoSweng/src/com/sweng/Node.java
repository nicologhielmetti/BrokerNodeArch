package com.sweng;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.json.simple.parser.ParseException;

import com.jsonrpc.*;

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
     * @param service
     */

    public boolean provideService(Service service) {
        IConnection  connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager(connection);
        JsonRpcRequest registerServiceRequest = new JsonRpcRequest("registerService", service.getServiceMetadata().toJson(), this.generateNewId());
        manager.send(registerServiceRequest);
        JsonRpcResponse registerServiceResponse = manager.listenResponse();

        // Read response from Broker
        JsonObject result = registerServiceResponse.getResult();
        boolean serviceRegistered = result.get("serviceRegistered").getAsBoolean();
        if (serviceRegistered) {
            String newMethodName = result.get("methodName").getAsString();
            service.getServiceMetadata().setMethodName(newMethodName);
        } else {
            // Timeout
        }
        // Start new service
        service.start();
        ownServices.put(service.getServiceMetadata().getMethodName(), service);
        return true;
    }

    public void deleteService(String method) { // missed in uml class diagram
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
            throw new RuntimeException("There is no service named " + method);
        }
    }

    public void setConncetionFactory(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // End of Service handler functionality

    // Begin of Service requester functionality


    public JsonRpcResponse requestService(String method, JsonObject parameters) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        JsonRpcRequest request = new JsonRpcRequest(method, parameters, generateNewId());
        manager.send(request);
        JsonRpcResponse response = manager.listenResponse();
        return response;
    }

    public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<>();
        list.clear();
        JsonRpcResponse response = this.requestService("getServicesList", searchStrategy.toJson());
        JsonObject json = response.getResult();
        JsonArray array = (JsonArray) json.get("result");
        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext()) {
            list.add(new ServiceMetadata(iterator.next().getAsJsonObject()));
        }
        return list;
    }

    private ID generateNewId() {
        return new ID(this.id++);
    }

    // End of Service requester functionality

}
