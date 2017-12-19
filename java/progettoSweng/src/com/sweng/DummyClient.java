package com.sweng;

import com.jsonrpc.JsonRpcResponse;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class DummyClient {


    public static void main(String[] args) {

        // Create new node obj
        //IConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://192.168.43.214:6789");
        IConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6789");
        Node node = new Node(connectionFactory);

        // Get list of available services from broker
        SearchStrategy search = new TitleSearchStrategy("sum");
        ArrayList<ServiceMetadata> serviceList = node.requestServiceList(search);

        if(serviceList.isEmpty()){
            System.out.println("Client: sum service not found");
        }

        for (ServiceMetadata s:serviceList) {
            System.out.println(s.toString());
        }

        // Invoke a service
        String method = "sum";
        JsonObject parameters = new JsonObject();
        parameters.addProperty("num1",36);
        parameters.addProperty("num2",6);
        JsonRpcResponse response = node.requestService(method, parameters);

        System.out.println("client received: " + response.toString());


    }

}
