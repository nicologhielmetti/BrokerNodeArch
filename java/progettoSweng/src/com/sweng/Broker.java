package com.sweng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

import com.jsonrpc.*;
import com.jsonrpc.Error;

import com.google.gson.*;
import org.json.simple.parser.ParseException;


//todo handle broker services as Service(s) (now they are hard-coded)
//todo change get__ to listen__ where necessary

public class Broker {

    private IConnectionManager connectionManager;
    private boolean isClosed;

    private Map<String, Service> brokerServices;

    private Map<String, JsonRpcManager> servers;
    private List<ServiceMetadata> services;

    /**
     * Generate a method name to identify unequivocally a Service.
     *
     * @param hint
     * @return
     */
    String generateMethodName(String hint) {
        if (servers.get(hint) == null) return hint;

        int i = 1;
        String generated;
        do {
            generated = hint + "@" + i;
            i++;
        } while (servers.get(generated) != null);

        return generated;
    }

    void registerService(JsonRpcRequest request, JsonRpcManager manager) {
        String name = request.getParams().get("title").toString();
        name = generateMethodName(name);

        ServiceMetadata serviceMetadata = new ServiceMetadata(request.getParams());
        serviceMetadata.setMethodName("title");
        servers.put(name, manager);
        services.add(serviceMetadata);

        JSONObject result = new JSONObject();
        result.put("serviceRegistered", "true");
        result.put("methodName", name);
        manager.sendResponse(new JsonRpcResponse(result, request.getId()));
    }

    String registerService(Service service) {
        String name = service.getServiceMetadata().getMethodName();
        name = generateMethodName(name);

        service.getServiceMetadata().setMethodName(name);
        brokerServices.put(name, service);
        services.add(service.getServiceMetadata());

        return name;
    }

    void deleteService(JsonRpcRequest request) {
        String name = request.getParams().get("title").toString();//todo non gestire con il nome ma con il riferimento alla connessione

        services.remove(name);
    }

    void handleServicesListRequest(JsonRpcRequest request, JsonRpcManager manager) {
        JSONObject j = (JSONObject) request.getParams().get("searchStrategy");
        List<ServiceMetadata> list;

        if (j.isEmpty()) list = getServicesList();
        else {
            SearchStrategy searchStrategy = SearchStrategy.create(j);
            if (searchStrategy == null) {
                manager.sendError(new Error("-32602", "SearchStrategy is ill-formed"), request.getId());
                return;
            }

            list = getServicesList(searchStrategy);
        }

        JSONObject result=new JSONObject();
        JSONArray l = new JSONArray();
        l.addAll(list);

        result.put("servicesList",l);

        manager.sendResponse(new JsonRpcResponse(result, request.getId()));
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
                    deleteService(request);
                    return false;
            }
        } else {
            switch (request.getMethod()) {
                case "registerService":
                    registerService(request, manager);
                    return false;
                case "getServicesList":
                    handleServicesListRequest(request, manager);
                    return false;
            }
        }
        return true;
    }


    void connectionThread(JsonRpcManager m) {

        if(verbose)System.out.println("connectionThread begin");

        JsonRpcRequest r = null;
        try {
            r = m.listenRequest();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(verbose)System.out.println("connectionThread: request="+r.toString());

        if (!filterRequest(r, m)) return;

        if (r.isNotification()) {
            //todo gestire le notifiche
        } else {

            JsonRpcManager server = servers.get(r.getMethod());

            if (server != null) {
                server.sendRequest(r);
                JsonRpcResponse res = null;
                try {
                    res = server.listenResponse();
                } catch (ParseException | NullPointerException e) {
                    e.printStackTrace();
                }

                m.sendResponse(res);
            } else {
                m.sendError(new Error("-32601","method not found"), r.getId());
                return;
            }
        }

    }

    void handleRequest(JsonRpcRequest request, JsonRpcManager manager){
        if (!filterRequest(request, manager)) return;

        if (request.isNotification()) {
            //todo gestire le notifiche
        } else {

            JsonRpcManager server = servers.get(request.getMethod());

            if (server != null) {
                server.sendRequest(request);
                JsonRpcResponse res = null;
                try {
                    res = server.listenResponse();
                } catch (ParseException | NullPointerException e) {
                    e.printStackTrace();
                }

                manager.sendResponse(res);
            } else {
                manager.sendError(new Error("-32601","method not found"), request.getId());
                return;
            }
        }
    }

    Broker(IConnectionManager connectionManager) {
        this.connectionManager = connectionManager;


    }


    void close(){
        isClosed=true;
    }

    final boolean verbose=true;

    void start() {
        isClosed=false;
        while (!isClosed) {

            if(verbose)System.out.println("Broker waiting for incoming connection...");

            IConnection c = connectionManager.acceptConnection();

            JsonRpcManager j = new JsonRpcManager(c);

            //new thread(connectionThread,j);

            if(verbose)System.out.println("Broker handling the request");

            Thread t1 = new Thread(() -> {
                try {
                    connectionThread(j);
                    if(verbose)System.out.println("Broker handled the request");
                } catch (Exception e) {
                    // handle: log or throw in a wrapped RuntimeException
                    throw new RuntimeException("InterruptedException caught in lambda", e);
                }
            });
            t1.start();
        }
    }
}
