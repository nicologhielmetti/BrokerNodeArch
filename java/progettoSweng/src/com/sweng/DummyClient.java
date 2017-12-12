package com.sweng;

import com.jsonrpc.JsonRpcResponse;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class DummyClient {


    public static void main(String[] args) {

        // Create new node obj
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
        JSONObject parameters = new JSONObject();
        parameters.put("num1",36);
        parameters.put("num2",6);
        JsonRpcResponse response=node.requestService(method, parameters);

        System.out.println("client received: "+response.toString());


    }

}
