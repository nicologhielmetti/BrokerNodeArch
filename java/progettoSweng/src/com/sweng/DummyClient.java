package com.sweng;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class DummyClient {


    public static void main(String[] args) {

        // Create new node obj
        IConnectionFactory connectionFactory = new IConnectionFactory();
        Node node = new Node(conncetionFactory);

        // Get list of available services from broker
        SearchStrategy search = null;
        ArrayList<ServiceMetadata> serviceList = node.requestServiceList(search);

        // Invoke a service
        String method = "method name";
        JSONObject parameters = null;
        node.requestService(method, parameters);

    }

}
