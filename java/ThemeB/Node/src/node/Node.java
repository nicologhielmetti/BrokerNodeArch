package node;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import connectioninterfaces.IConnection;
import connectioninterfaces.IConnectionFactory;
import javafx.util.Pair;
import jsonrpclibrary.*;
import searchstrategy.SearchStrategy;
import service.IServiceMethod;
import service.JsonRpcCustomError;
import service.Service;
import service.ServiceMetadata;

import java.util.*;

// todo timer on response

/**
 *  This class contains server side and client side function because when can have a single instance of Node as node
 *  that can do a client, a server or both.
 */

public class Node {

    private Map<String, Service> ownServices; /** Service that are provided by a node */
    private IConnectionFactory connectionFactory; /** It is used to create new connection */
    private int id; /** Every JSON-RPC request form a node have a different jsonrpclibrary.ID */

    // Start of Service handler functionality

    /**
     * Dictionary:
     * - Server ---> DummyServer.java
     * - Client ---> DummyClient.java
     */

    /**
     * Node is the constructor of the Node class.
     * The IConncetionFactory parameter is used to define which is used to define
     * the connection that this object have to use.
     * @param connectionFactory
     */

    public Node(IConnectionFactory connectionFactory) {
        this.id = 0;
        this.connectionFactory = connectionFactory;
        ownServices = new HashMap<>();
    }

    /**
     * provideService is public method that allow the application layer (servers) to provide
     * an own service to all clients connected in the system. With this method the server send
     * to the broker a request of adding service ("registerService") and the broker if the operation
     * is successful a response is return with the correct method name in order not to have duplicated
     * method identifier.
     * When the service is correctly published this method start a thread that wait requests from clients.
     * @param metadata, function
     */

    public boolean provideService(ServiceMetadata metadata, IServiceMethod function) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        Service service = new Service(metadata, function, manager);
        JsonRpcRequest registerServiceRequest = new JsonRpcRequest("registerService", metadata.toJson(), this.generateNewId());
        manager.send(registerServiceRequest);
        JsonRpcResponse registerServiceResponse = null;
        try {
            registerServiceResponse = (JsonRpcResponse) manager.listenResponse();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Read response from broker
        JsonObject result = registerServiceResponse.getResult().getAsJsonObject();
        boolean serviceRegistered = result.get("serviceRegistered").getAsBoolean();
        if (serviceRegistered) {
            String newMethodName = result.get("method").getAsString();
            metadata.setMethodName(newMethodName);
        } else {
            // Timeout
        }
        System.out.println("Server: Service registered!");
        // Start new service
        service.start();
        ownServices.put(metadata.getMethodName(), service);
        return true;
    }

    /**
     * deleteService is a public api that allow to the service owner to delete a service.
     * * This method send a particular JSON-RPC notification wih method = "deleteService", that is a particular service
     * inside the system broker, and a parameter that is the name of the method (Service) that must be deleted.
     * No response is  expected because broker must delete the service.
     * If there id no service provided by node with methodName = method an error is returned.
     * @param method
     */

    public void deleteService(String method) {
        if (this.ownServices.containsKey(method)) {
            IConnection connection = this.connectionFactory.createConnection();
            JsonRpcManager manager = new JsonRpcManager(connection);

            JsonObject jsonMethod = new JsonObject();
            jsonMethod.addProperty("method", method);

            JsonRpcRequest request = JsonRpcRequest.notification("deleteService", jsonMethod);
            manager.send(request);
            // Delete service
            Service availableService = this.ownServices.get(method);
            availableService.interrupt();
            availableService.delete();
            this.ownServices.remove(method);
        } else {
            System.err.println("Server: There is no service named " + method);
        }
    }

    /**
     * This api is used to allow the node user to change at runtime the connection implementation used to connect to
     * broker that want to use. For example a node can change ip and port used.
     * @param connectionFactory
     */

    public void setConncetionFactory(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // End of Service handler functionality

    // Begin of Service requester functionality

    /**
     * requestService is a public api used to send single request to a service registered in the system broker.
     * This method send a JSON-RPC request and wait for JSON-RPC response :
     * - If the response is correctly received is returned to the requester.
     * - If the requested service is not available in the broker a JSON-RPC jsonrpclibrary.Error method not found is received as response.
     * - If the received JSON-RPC response can't be correctly parsed from the requester a custom JSON-RPC jsonrpclibrary.Error is returned
     *   and an error is printed to console.
     * - If timeout occurred, a custom error is returned. todo timeout!!!!!
     * @param method
     * @param parameters
     * @return
     */

    public JsonRpcResponse requestService(String method, JsonElement parameters) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        JsonRpcRequest request = new JsonRpcRequest(method, parameters, generateNewId());
        manager.send(request);
        JsonRpcResponse response = null;
        try {
            response = (JsonRpcResponse) manager.listenResponse();
        } catch (ParseException e) {
            System.err.println("Client: Local parse exeption: " + response.toString());
            response = JsonRpcResponse.error(JsonRpcCustomError.localParseError(), ID.Null());
        }
        return response;
    }

    /**
     * requestService is a public api used to send batch request to one or more service registered in the system broker.
     * This method send a JSON-RPC batch request and wait for JSON-RPC batch response :
     * - If the response is correctly received is returned to the requester.
     * - If the requested service is not available in the broker a JSON-RPC jsonrpclibrary.Error method not found is received as response.
     * - If the received JSON-RPC response can't be correctly parsed from the requester a custom JSON-RPC jsonrpclibrary.Error is returned
     *   and an error is printed to console.
     * - If timeout occurred, a custom error is returned. todo timeout!!!!!
     * @param methodsAndParameters
     * @return
     */

    public JsonRpcBatchResponse requestService(ArrayList<Pair<String, JsonElement>> methodsAndParameters) {
        JsonRpcManager manager = new JsonRpcManager(this.connectionFactory.createConnection());
        JsonRpcBatchRequest requests = new JsonRpcBatchRequest();
        for (Pair<String, JsonElement> request: methodsAndParameters) {
            requests.add(new JsonRpcRequest(request.getKey(), request.getValue(), generateNewId()));
        }
        manager.send(requests);
        JsonRpcBatchResponse responses = new JsonRpcBatchResponse();
        try {
            responses = (JsonRpcBatchResponse) manager.listenResponse();
        } catch (ParseException e) {
            System.err.println("Client: Local parse exeption: " + responses.toString());
            responses.add(JsonRpcResponse.error(JsonRpcCustomError.localParseError(), ID.Null()));
        }
        return responses;
    }

    /**
     * requestServiceList is a public api used to retrieve all services registered in the system broker.
     * This method send a particular JSON-RPC request wih method = "getServicesList", that is a particular service inside
     * the system broker, and a parameter that is a searchStrategy that is an object that allow the user to define the
     * type of serch that want perform. For more information see broker and SearchStrategy class.
     * @param searchStrategy
     * @return
     */

   public ArrayList<ServiceMetadata> requestServiceList(SearchStrategy searchStrategy) {
        ArrayList<ServiceMetadata> list = new ArrayList<>();
        list.clear();
        JsonRpcResponse response = this.requestService("getServicesList", searchStrategy.toJsonElement());
        JsonArray array = response.getResult().getAsJsonArray();
        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext()) {
            list.add(ServiceMetadata.fromJson(iterator.next().getAsJsonObject()));
        }
        return list;
    }

    /** This method print to console (for debug purpose) all service that are correctly published by the node */
    public void showRunningServices() {
        Iterator<Map.Entry<String, Service>> i = ownServices.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String,Service> it = i.next();
            System.out.println("Name: " + it.getKey() + " - " + it.getValue().getServiceMetadata().toJson());
        }
    }

    // End of Service requester functionality

    /**
     * generateNewId is private a  method that increment the id every time a request is generated
     * @return
     * */

     private ID generateNewId() {
        return new ID(this.id++);
    }

}
