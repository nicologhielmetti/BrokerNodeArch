package com.sweng;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.jsonrpc.JsonRpcRequest;
import com.jsonrpc.JsonRpcResponse;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Scanner;

public class DummyNode {

    private static Node node;
    private static Scanner keyboard = new Scanner(System.in);
    private static IServiceMethod methodSum;
    private static IServiceMethod methodMul;
    private static ServiceMetadata metadataSum;
    private static ServiceMetadata metadataMul;

    public static void main(String[] args) {

        // Create new node obj
        //IConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://192.168.43.214:6789");
        IConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6789");
        node = new Node(connectionFactory);

        setupExampleService();

        // User interface
        int key;
        do {
            System.out.println("Please select one option: ");
            System.out.println("1 - Get list of available services");
            System.out.println("2 - Invoke a service");
            System.out.println("3 - Provide a own service");
            System.out.println("4 - Delete a own service");
            System.out.println("5 - Exit");
            System.out.print("> ");
            key = keyboard.nextInt();
            switch (key) {
                case 1 : getServiceList(); break;
                case 2 : invokeService(); break;
                case 3 : provideService(); break;
                case 4 : deleteService(); break;
                case 5 : break;
                case 6 : node.showRunningServices(); break;
                default : System.out.println("No option found"); break;
            }
        } while (key != 5);
    }

    // Get list of available services from broker (Client side function)
    private static void getServiceList() {
        System.out.println("Please select a type of search : ");
        System.out.println("1 - Title");
        System.out.println("2 - Owner");
        System.out.println("3 - Keyword");
        System.out.print("> ");
        int key = keyboard.nextInt();
        SearchStrategy search;
        switch (key) {
            case 1 :
                System.out.print("Please insert title > ");
                search = new TitleSearchStrategy(keyboard.next());
                break;
            case 2 :
                System.out.print("Please insert a service owner > ");
                search = new OwnerSearchStrategy(keyboard.next());
                break;
            case 3 :
                System.out.print("Please insert a keyword space separated > ");
                String keyword = keyboard.next();
                search = new KeywordSearchStrategy(keyword);
                break;
            default :
                System.out.println("No option found");
                return;
        }
        ArrayList<ServiceMetadata> serviceList = node.requestServiceList(search);
        if (serviceList.isEmpty()) {
            System.out.println("Client: no result from search");
        }
        for (ServiceMetadata s : serviceList) {
            System.out.println(s.toJson());
        }
    }

    // Make a service request - invoke a service (Client side function)
    private static void invokeService() {
        System.out.print("Insert method : ");
        String method = keyboard.next();
        System.out.print("Insert params (property:value) comma separated (leave empty if no parameters needed) : ");
        String params = keyboard.next();
        List<String> parameters = new ArrayList<>(Arrays.asList(params.split(",")));
        JsonObject jsonParameters = new JsonObject();
        for (String s: parameters){
            String[] param = s.split(":");
            jsonParameters.addProperty(param[0], param[1]); // fix error please (exeption)
        }
        JsonRpcResponse response = node.requestService(method, jsonParameters);
        System.out.println("client received: " + response.toString());
    }

    // Provide a own service (Server side function)
    private static void provideService() {
        System.out.print("Please insert the name of the service you want to add (sum or mul) > ");
        String option = keyboard.next();
        if (option.equals("sum")) {
            node.provideService(metadataSum, methodSum);
        }
        else if (option.equals("mul")) {
            node.provideService(metadataMul, methodMul);
        }
    }

    // Delete a own service (Server side function)
    private static void deleteService() {
        System.out.print("Please insert the name of the service you want to delete > ");
        String methodName = keyboard.next();
        node.deleteService(methodName);
    }

    private static void setupExampleService() {
        // sum service
        metadataSum = new ServiceMetadata("sum", "DummyNode");
        metadataSum.setKeywords(new ArrayList<>(Arrays.asList("somma", "sum", "sommatoria")));
        metadataSum.setApplicationField("math");
        metadataSum.setDescription("input (num1:int_value,...,numN:int_value)");
        // Set all metadata through setter methods
        methodSum = new IServiceMethod() {
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
        System.out.println("Service created");


        // mul service
        metadataMul = new ServiceMetadata("mul", "DummyNode");
        metadataMul.setKeywords(new ArrayList<>(Arrays.asList("moltiplicazione", "mul")));
        metadataMul.setApplicationField("math");
        metadataMul.setDescription("input (num1:int_value,...,numN:int_value)");
        // Set all metadata through setter methods
        methodMul = new IServiceMethod() {
            @Override
            public JsonRpcResponse run(JsonRpcRequest request) {
                JsonRpcResponse response = null;
                try {
                    System.out.println("Service is running...");
                    JsonObject parameters = request.getParams().getAsJsonObject();
                    int result = 1;
                    for (int i = 1; i <= parameters.size(); i++) {
                        result *= parameters.get("num" + String.valueOf(i)).getAsInt();
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
        System.out.println("Service created");
    }

}
