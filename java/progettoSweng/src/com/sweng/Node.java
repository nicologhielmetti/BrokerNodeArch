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

    // Start of Service handler functionality

    /**
     * Node is the constructor of the Node class
     * @param connectionFactory
     */

    public Node(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        ownServices = new HashMap<>();
    }

    public void provideService(Service service) {
        IConnection  connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager(connection);
        JsonRpcRequest request = new JsonRpcRequest("registerService", service.getServiceMetadata().toJson(), 0);
        manager.sendRequest(request);
        JsonRpcResponse response = manager.getResponse();
        // Thread creation
        Thread thread = new Thread(() -> {
            try {
                // Wait request
                service.processRequest(null);
                // Send response
            } catch (RuntimeException e) {
                throw new RuntimeException("Provide service thread");
            }
        });

        ownServices.put(service.getServiceMetadata().getTitle(), new Triple(Service service, JsonRpcManager manager, Thread thread));
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

    }

    public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<ServiceMetadata>();
        list.clear();

        IConnection connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager();
        JsonRpcRequest request = new JsonRpcRequest("searchStrategy", searchStrategy.toJson(), 0);
        manager.sendRequest(request);
        JsonRpcResponse response = manager.getResponse();

        JSONObject json = response.getJsonRpc();
        JSONArray array = (JSONArray) json.get("result");
        Iterator<JSONObject> iterator = array.iterator();
        while (iterator.hasNext()) {
            list.add(new ServiceMetadata(iterator.next()));
        }

        return list;
    }


    // End of Service requester functionality

}
