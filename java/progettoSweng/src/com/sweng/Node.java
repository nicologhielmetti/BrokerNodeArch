package com.sweng;

import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.jsonrpc.*

public class Node {

    private Map<String, Service> ownServices;
    private IConnectionFactory connectionFactory;

    // Start of Service handler functionality

    public Node(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        ownServices = new HashMap<>();
    }

    public void provideService(Service service) {
        ServiceMetadata metadata = service.getServiceMetadata();
        ownServices.put(metadata.getTitle(), service);
        IConnection  connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager(connection);

        JsonRpcRequest request = new JsonRpcRequest();

        manager.sendRequest(request);
        JsonRpcResponse response = manager.getResponse();

        // Create JsonRpcRequest
        // Send new service metadata to broker
    }

    public void deleteService(String method) { // missed in uml class diagram
        IConnection connection = this.connectionFactory.createConnection();
        JsonRpcManager manager = new JsonRpcManager(connection);

        JsonRpcRequest notification = new JsonRpcRequest();
        notification.isNotification();

        this.ownServices.remove(method);
    }

    public void setConncetionFactory(IConnectionFactory conncetionFactory) {
        this.connectionFactory = conncetionFactory;
    }

    // End of Service handler functionality

    // Begin of Service requester functionality


    public void requestService(String method, JSONObject parameters) {

    }

    public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<ServiceMetadata>();
        return list;
    }


    // End of Service requester functionality

}
