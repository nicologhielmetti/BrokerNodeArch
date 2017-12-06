package com.sweng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

import com.jsonrpc.*;
import com.jsonrpc.Error;

//todo handle broker services as Service(s) (now they are hard-coded)


public class Broker {

    private IConnectionManager connectionManager;

    private Map<String, JsonRpcManager> servers;
    private List<ServiceMetadata> services;

    /**
     * Generate a method name to identify unequivocally a Service.
     *
     * @param hint
     * @return
     */
    String generateMethodName(String hint) {
        if (services.get(hint) == null) return hint;

        int i = 1;
        String generated;
        do {
            generated = hint + "@" + i;
            i++;
        } while (services.get(hint) != null);

        return generated;
    }

    void registerService(JsonRpcRequest request, JsonRpcManager manager) {
        String name = request.getParams().get("title");
        name = generateMethodName(name);

        ServiceMetadata serviceMetadata = new ServiceMetadata(request.getParams());
        serviceMetadata.setTitle("title");
        servers.put(name, manager);
        services.add(serviceMetadata);

        JSONObject result = new JSONObject();
        result.put("serviceRegistered", "true");
        result.put("methodName", name);
        manager.sendResponse(new JsonRpcResponse(result, request.getID()));
    }

    void deleteService(JsonRpcRequest request) {
        String name = request.getParams().get("title");

        services.remove(name);
    }

    void handleServicesListRequest(JsonRpcRequest request,JsonRpcManager manager) throws InvalidRequestException {
        JSONObject j = request.getParams().get("searchStrategy");
        List<ServiceMetadata> list;

        if (j.isEmpty()) list = getServicesList();
        else {
            SearchStrategy searchStrategy = SearchStrategy.create(j);
            if (searchStrategy == null) {
                manager.sendError(new Error(-32602,"SearchStrategy is ill-formed"),request.getID());
                return;
            }

            list = getServicesList(searchStrategy);
        }

        JSONArray result = new JSONArray();
        result.addAll(list);

        manager.sendResponse(new JsonRpcResponse(result,request.getID()));
    }

    List<ServiceMetadata> getServicesList() {
        return services;
    }

    List<ServiceMetadata> getServicesList(SearchStrategy searchStrategy) {
        return searchStrategy.filterList(services);
    }

    boolean filterRequest(JsonRpcRequest request, JsonRpcManager manager) {
        if (request.isNotification()) {
            switch (request.getMethod()) {
                case "deleteService":
                    deleteService(request, manager);
                    return false;
            }
        } else {
            switch (request.getMethod()) {
                case "registerService":
                    registerService(request, manager);
                    return false;
                case "getServicesList":
                    handleServicesListRequest(request,manager);
                    return false;
            }
        }
        return true;
    }


    void connectionThread(JsonRpcManager m) {

        JsonRpcRequest r = m.getRequest();

        if (!filterRequest(r, m)) return;

        if (r.isNotification()) {
            //todo gestire le notifiche
        } else {

            JsonRpcManager server = services.get(r.getMethod());

            if (server != null) {
                server.sendRequest(r);
                JsonRpcResponse res = server.getResponse();

                m.sendResponse(res);
            } else {
                return m.sendError("method not found", r.getID());
            }
        }

    }

    Broker(IConnectionManager connectionManager){
        this.connectionManager=connectionManager;


    }


    void exec() {

        while (true) {

            IConnection c = connectionManager.getIncomingConnection();

            JsonRpcManager j = new JsonRpcManager(c);

            //new thread(connectionThread,j);

            Thread t1 = new Thread(() -> {
                try {
                    connectionThread(j);
                } catch (Exception e) {
                    // handle: log or throw in a wrapped RuntimeException
                    throw new RuntimeException("InterruptedException caught in lambda", e);
                }
            });
        }
    }
}
