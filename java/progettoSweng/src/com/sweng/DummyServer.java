package com.sweng;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jsonrpc.JsonRpcManager;
import com.jsonrpc.JsonRpcRequest;
import com.jsonrpc.JsonRpcResponse;
import com.jsonrpc.Error;
import com.google.gson.JsonElement;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class DummyServer {

    public static void main(String[] args) {

        // Create node obj
        //ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://192.168.43.214:6789");
        ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6789");
        Node node = new Node(connectionFactory);

        // Provide a service
        ServiceMetadata serviceMetadata = new ServiceMetadata("sum", "owner");
        serviceMetadata.setKeywords(new ArrayList<String>(Arrays.asList("somma", "sum", "sommatoria")));
        serviceMetadata.setApplicationField("math");
        serviceMetadata.setDescription("input (num1:value,...,numN:value)");
        // Set all metadata through setter methods
        IServiceMethod serviceMethod = new IServiceMethod() {
            @Override
            public JsonRpcResponse run(JsonRpcRequest request) {
                JsonRpcResponse response = null;
                try {
                    System.out.println("Service is running...");
                    JsonObject parameters = request.getParams().getAsJsonObject();
                    int result = 0;
                    for (int i = 1; i <= parameters.size(); i++) {
                        result += parameters.get("num" + String.valueOf(i)).getAsInt();
                    }
                    System.out.println(result);

                    if (request.isNotification()) {
                        response = null;
                    } else {
                        JsonPrimitive resultJson = new JsonPrimitive(result);
                        response = new JsonRpcResponse(resultJson, request.getID());
                    }
                }
                catch (IllegalArgumentException e) {
                    response = JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
                    System.err.println("Wrong JSON-RPC Request received, a JSON-RPC Error is returned to requester");
                }
                finally {
                    System.out.println("Service finish");
                    return response;
                }
            }
        };
        //Service service = new Service(serviceMetadata, serviceMethod);
        System.out.println("Service created");

        node.provideService(serviceMetadata, serviceMethod);
/*
        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Delete an own service
        node.deleteService(serviceMetadata.getMethodName());*/
    }

}
