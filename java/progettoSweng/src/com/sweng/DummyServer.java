package com.sweng;

import com.jsonrpc.JsonRpcRequest;
import com.jsonrpc.JsonRpcResponse;
import com.jsonrpc.Error;
import org.json.simple.JSONObject;

public class DummyServer {

    public static void main(String[] args) {

        // Create node obj
        ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://192.168.43.214:6789");
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
                    JSONObject parameters = request.getParams();
                    int num1 = Integer.parseInt(parameters.get("num1").toString());
                    int num2 = Integer.parseInt(parameters.get("num2").toString());
                    int result = num1 + num2;
                    System.out.println(result);

                    if (request.isNotification()) {
                        response = null;
                    } else {
                        JSONObject resultJsonObject = new JSONObject();
                        resultJsonObject.put("result", Integer.toString(result));
                        response = new JsonRpcResponse(resultJsonObject, request.getId());
                    }
                }
                catch (IllegalArgumentException e) {
                    response = new JsonRpcResponse(new Error("-32603", "Wrong parameters received"), request.getId());
                    System.err.println("Wrong JSON-RPC Request received, a JSON-RPC Error is returned to requester");
                }
                finally {
                    System.out.println("Service finish");
                    return response;
                }
            }
        };
        Service service = new Service(serviceMetadata, serviceMethod);
        System.out.println("Service created");

        node.provideService(service);

        // Delete an own service
        //node.deleteService(service.getServiceMetadata().getMethodName());
    }

}
