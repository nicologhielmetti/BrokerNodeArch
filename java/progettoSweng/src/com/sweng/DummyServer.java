package com.sweng;

import com.google.gson.JsonObject;
import com.jsonrpc.JsonRpcManager;
import com.jsonrpc.JsonRpcRequest;
import com.jsonrpc.JsonRpcResponse;
import com.jsonrpc.Error;
import com.google.gson.JsonElement;

import java.util.concurrent.TimeUnit;

public class DummyServer {

    public static void main(String[] args) {

        // Create node obj
        //ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://192.168.43.214:6789");
        ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6789");
        Node node = new Node(connectionFactory);

        // Provide a service
        ServiceMetadata serviceMetadata = new ServiceMetadata("sum", "owner");
        // Set all metadata through setter methods
        IServiceMethod serviceMethod = new IServiceMethod() {
            @Override
            public JsonRpcResponse run(JsonRpcRequest request) {
                JsonRpcResponse response = null;
                try {
                    System.out.println("Service is running...");
                    JsonObject parameters = request.getParams().getAsJsonObject();
                    int num1 = Integer.parseInt(parameters.get("num1").toString());
                    int num2 = Integer.parseInt(parameters.get("num2").toString());
                    int result = num1 + num2;
                    System.out.println(result);

                    if (request.isNotification()) {
                        response = null;
                    } else {
                        JsonObject resultJsonObject = new JsonObject();
                        resultJsonObject.addProperty("result", result);
                        response = new JsonRpcResponse(resultJsonObject, request.getID());
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

        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Delete an own service
        node.deleteService(serviceMetadata.getMethodName());
    }

}
