package com.sweng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.json.simple.parser.ParseException;

import com.jsonrpc.*;


public class Node {

    private Map<String, RunningService> ownServices;
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
        JsonRpcRequest request = new JsonRpcRequest("registerService", service.getServiceMetadata().toJson(), this.generateNewId());
        manager.sendRequest(request);
        JsonRpcResponse response = null;
        try {
            response = manager.listenResponse();
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }

        // Read response from Broker
        JSONObject result = response.getResult();
        boolean serviceRegistered = (boolean)result.get("serviceRegistered");
        if (serviceRegistered) {
            String newMethodName = (String) result.get("methodName");
            service.getServiceMetadata().setMethodName(newMethodName);
        } else {
            // Exeption
        }
        // Thread creation
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    // Wait request
                    JsonRpcRequest receivedRpcRequest = null;
                    try {
                        receivedRpcRequest = manager.listenRequest();
                    } catch (ParseException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    JSONObject parameters = receivedRpcRequest.getParams();
                    //Check service signature
                    //Execute request
                    JSONObject serviceResult = service.processRequest(parameters);
                    // Send response
                    JsonRpcResponse generatedResponse = new JsonRpcResponse(serviceResult, receivedRpcRequest.getId());
                    manager.sendResponse(generatedResponse);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Provide service thread");
                }
            }
        });
        thread.start();

        ownServices.put(service.getServiceMetadata().getMethodName(), new RunningService(service, manager, thread));
    }

    public void deleteService(String method) { // missed in uml class diagram
        IConnection connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager(connection);
        manager.sendNotification("deleteService");

        // Delete service
        RunningService availableService = this.ownServices.get(method);
        availableService.delete();
        this.ownServices.remove(method);
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
        JsonRpcResponse response = null;
        try {
            response = manager.listenResponse();
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
        return response;
    }

    public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<ServiceMetadata>();
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
