package com.sweng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import com.jsonrpc.*;


public class Node {

    private Map<String, Triple<Service, JsonRpcManager, Thread>> ownServices;
    private IConnectionFactory connectionFactory;
    private int id;

    // Start of Service handler functionality

    /**
     * Node is the constructor of the Node class
     * @param connectionFactory
     */

    public Node(IConnectionFactory connectionFactory) {
        this.id = 0;
        this.connectionFactory = connectionFactory;
        ownServices = new HashMap<>();
    }

    public void provideService(Service service) {
        IConnection  connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager(connection);
        JsonRpcRequest request = new JsonRpcRequest("registerService", service.getServiceMetadata().toJson(), 0);
        manager.sendRequest(request);
        JsonRpcResponse response = manager.getResponse();
        // Read response from Broker
        JSONObject result = response.getResult();
        boolean serviceRegistered = (boolean) result.get("serviceRegistered");
        if (serviceRegistered) {
            String newMethodName = (String) result.get("methodName");
            service.getServiceMetadata().setMethodName(newMethodName);
        } else {
            // Exeption
        }
        // Thread creation
        Thread thread = new Thread(() -> {
            try {
                // Wait request
                JsonRpcRequest receivedRequest = manager.listenRequest();
                JSONObject json = service.processRequest(receivedRequest);
                // Send response
            } catch (RuntimeException e) {
                throw new RuntimeException("Provide service thread");
            }
        });
        thread.start();

        ownServices.put(service.getServiceMetadata().getMethodName(), new Triple<>(service,manager, thread));
    }

    public void deleteService(String method) { // missed in uml class diagram
        IConnection connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager(connection);
        manager.sendNotification();

        this.ownServices.remove(method);
    }

    public void setConncetionFactory(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // End of Service handler functionality

    // Begin of Service requester functionality


    public JsonRpcResponse requestService(String method, JSONObject parameters) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(generateNewId());
        request.setMethod(method);
        request.setParams(parameters);
        manager.sendRequest(request);
        JsonRpcResponse response = manager.getResponse();
        return response;
    }

    public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<ServiceMetadata>();
        list.clear();

        JsonRpcResponse response = this.requestService("searchStrategy", searchStrategy.toJson());

        JSONObject json = response.getJsonRpc();
        JSONArray array = (JSONArray) json.get("result");
        Iterator<JSONObject> iterator = array.iterator();
        while (iterator.hasNext()) {
            list.add(new ServiceMetadata(iterator.next()));
        }
        return list;
    }

    private int generateNewId() {
        return this.id;
    }

    // End of Service requester functionality

}
