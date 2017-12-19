package com.sweng;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jsonrpc.*;
import com.google.gson.*;
import com.jsonrpc.Error;


//todo handle broker services as Service(s) (now they are hard-coded)
//todo change get__ to listen__ where necessary

public class Broker {

    private IConnectionManager connectionManager;
    private boolean isClosed;

    private Map<String, Service> brokerServices=new HashMap<>();

    private Map<String, JsonRpcManager> servers=new HashMap<>();
    private List<ServiceMetadata> services=new LinkedList<>();

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
        JsonObject params= request.getParams().getAsJsonObject();

        if(params==null)throw new RuntimeException("failed to register a service: title not found");

        String name=params.get("methodName").getAsString();
        if(name.isEmpty())throw new RuntimeException("failed to register a service: title not found");


        name = generateMethodName(name);

        if(verbose)System.out.println("registerService generated name = "+name);

        ServiceMetadata serviceMetadata = ServiceMetadata.fromJson(request.getParams().getAsJsonObject());
        serviceMetadata.setMethodName("title");
        servers.put(name, manager);
        services.add(serviceMetadata);

        if(verbose)System.out.println("registerService service registered - communicating to node");

        JsonObject result = new JsonObject();
        result.addProperty("serviceRegistered", true);
        result.addProperty("methodName", name);
        manager.send(new JsonRpcResponse(result, request.getID()));

        if(verbose)System.out.println("registerService done");
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
        String name = request.getParams().getAsJsonObject().get("method").toString();//todo non gestire con il nome ma con il riferimento alla connessione

        services.remove(name);
    }

    void handleServicesListRequest(JsonRpcRequest request, JsonRpcManager manager) {
        JsonObject j = request.getParams().getAsJsonObject();
        List<ServiceMetadata> list;

        if (j==null) list = getServicesList();
        else {
            SearchStrategy searchStrategy = SearchStrategy.fromJson(j.toString());
            if (searchStrategy == null) {
                manager.send(JsonRpcResponse.error(new Error(-32602, "SearchStrategy is ill-formed"), request.getID()));
                return;
            }
            list = getServicesList(searchStrategy);
        }

        //JSONObject result=new JSONObject();
        JsonArray result = new JsonArray();
        for(ServiceMetadata s:list){
            result.add(s.toJson());
        }

        //result.put("servicesList",l);

        if(verbose)System.out.println("generated list (result):"+result.toString());
        if(verbose)System.out.println("id:"+request.getID());
        manager.send(new JsonRpcResponse(result, request.getID()));
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
                    if(verbose)System.out.println("registerService request");
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

        JsonRpcMessage r = null;
        try {
            r = m.listenRequest();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if(r instanceof JsonRpcRequest) {
            JsonRpcRequest request = (JsonRpcRequest) r;

            if (verbose)
                System.out.println("connectionThread: methodName=\"" + request.getMethod() + "\"\trequest=" + request.toString());

            if (!filterRequest(request, m)) return;

            if (request.isNotification()) {
                //todo gestire le notifiche
            } else {

                JsonRpcManager server = servers.get(request.getMethod());

                if (server != null) {
                    server.send(request);
                    JsonRpcMessage res = null;
                    try {
                        res = server.listenResponse();
                    } catch (ParseException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    m.send(res);
                } else {
                    m.send(JsonRpcDefaultError.methodNotFound(request.getID()));
                    return;
                }
            }
        }else if(r instanceof JsonRpcBatchRequest){

        }else{
            //error
        }

    }

    void handleRequest(JsonRpcRequest request, JsonRpcManager manager){
        if (!filterRequest(request, manager)) return;

        if (request.isNotification()) {
            //todo gestire le notifiche
        } else {

            JsonRpcManager server = servers.get(request.getMethod());

            if (server != null) {
                server.send(request);
                JsonRpcMessage res = null;
                try {
                    res = server.listenResponse();
                } catch (ParseException | NullPointerException e) {
                    e.printStackTrace();
                }

                manager.send(res);
            } else {
                manager.send(JsonRpcDefaultError.methodNotFound(request.getID()));
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
