package com.sweng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.json.simple.parser.ParseException;

import com.jsonrpc.*;


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
        manager.sendRequest(registerServiceRequest);
        JsonRpcResponse registerServiceResponse = (JsonRpcResponse) manager.listenResponse();

        // Read response from Broker
        JSONObject result = registerServiceResponse.getResult();
        boolean serviceRegistered = (boolean) result.get("serviceRegistered");
        if (serviceRegistered) {
            String newMethodName = (String) result.get("methodName");
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
            manager.sendNotification("deleteService");
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


    public JsonRpcResponse requestService(String method, JSONObject parameters) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        JsonRpcRequest request = new JsonRpcRequest(method, parameters, generateNewId());
        manager.sendRequest(request);
        JsonRpcResponse response = (JsonRpcResponse) manager.listenResponse();
        return response;
    }

    public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<>();
        list.clear();
        JsonRpcResponse response = this.requestService("getServicesList", searchStrategy.toJson());
        JSONObject json = response.getJsonRpc();
        JSONArray array = (JSONArray) json.get("result");
        Iterator<JSONObject> iterator = array.iterator();
        while (iterator.hasNext()) {
            list.add(new ServiceMetadata(iterator.next()));
        }
        return list;
    }

    private ID generateNewId() {
        return new ID(this.id++);
    }

    // End of Service requester functionality

}
