package com.sweng;

import java.util.*;

import com.jsonrpc.*;
import com.google.gson.*;
import com.jsonrpc.Error;


//todo handle broker services as Service(s) (now they are hard-coded)
//todo change get__ to listen__ where necessary

public class Broker {

    private IConnectionManager connectionManager;
    private boolean isClosed;

    private Map<String, IServiceMethod> brokerServices = new HashMap<>();

    private Map<String, JsonRpcManager> servers = new HashMap<>();
    private List<ServiceMetadata> services = new LinkedList<>();

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

    JsonRpcResponse registerService(JsonRpcRequest request, JsonRpcManager manager) {
        JsonObject params = request.getParams().getAsJsonObject();

        if (params == null) throw new RuntimeException("failed to register a service: title not found");

        String name = params.get("method").getAsString();
        if (name.isEmpty()) throw new RuntimeException("failed to register a service: title not found");


        name = generateMethodName(name);

        if (verbose) System.out.println("registerService: generated name = " + name);

        ServiceMetadata serviceMetadata = ServiceMetadata.fromJson(request.getParams().getAsJsonObject());
        serviceMetadata.setMethodName(name);
        servers.put(name, manager);
        services.add(serviceMetadata);

        if (verbose) System.out.println("registerService: service registered");

        JsonObject result = new JsonObject();
        result.addProperty("serviceRegistered", true);
        result.addProperty("method", name);

        return new JsonRpcResponse(result, request.getID());
    }

    /*
    void deleteService(JsonRpcRequest request) {
        String name = request.getParams().getAsJsonObject().get("method").getAsString();
        for (ServiceMetadata s : services) {
            if (s.getMethodName().equals(name)) {
                services.remove(s);
                this.servers.remove(name);
                return;
            }
        }
    }

    void handleServicesListRequest(JsonRpcRequest request, JsonRpcManager manager) {
        JsonObject j = request.getParams().getAsJsonObject();
        List<ServiceMetadata> list;

        if (j == null) list = getServicesList();
        else {
            SearchStrategy searchStrategy = SearchStrategy.fromJson(j.toString());
            if (searchStrategy == null) {
                manager.send(JsonRpcResponse.error(JsonRpcCustomError.wrongSerchStrategy(), request.getID()));
                return;
            }
            list = getServicesList(searchStrategy);
        }

        JsonArray result = new JsonArray();
        for (ServiceMetadata s : list) {
            result.add(s.toJson());
        }

        if (verbose) System.out.println("generated list (result):" + result.toString());
        if (verbose) System.out.println("id:" + request.getID());
        manager.send(new JsonRpcResponse(result, request.getID()));
    }
    */
    List<ServiceMetadata> getServicesList() {
        return services;
    }

    List<ServiceMetadata> getServicesList(SearchStrategy searchStrategy) {
        return searchStrategy.filterList(services);
    }

    boolean filterRequest(JsonRpcRequest request, JsonRpcManager manager, JsonRpcResponse response) {
        IServiceMethod service = brokerServices.get(request.getMethod());

        if (service != null) {
            response=service.run(request);
            return true;
        }

        if(request.getMethod().equals("registerService")){
                    if (verbose) System.out.println("registerService request");
                    response=registerService(request, manager);
                    return true;
        }
        return false;
    }


    void connectionThread(JsonRpcManager m) {

        if (verbose) System.out.println("connectionThread begin");

        JsonRpcMessage r = null;
        try {
            r = m.listenRequest();
        } catch (ParseException e) {
            //e.printStackTrace();
            m.send(JsonRpcDefaultError.parseError());
            return;
        }

        if (r instanceof JsonRpcRequest) {

            JsonRpcResponse response=handleRequest((JsonRpcRequest)r,m);
            if(response!=null) m.send(response);

        } else if (r instanceof JsonRpcBatchRequest) {
            JsonRpcBatchRequest requestBatch = (JsonRpcBatchRequest) r;
            JsonRpcBatchResponse responseBatch = new JsonRpcBatchResponse();

            for (JsonRpcRequest request : requestBatch.get()) {
                if (request.isValid()) {
                    JsonRpcResponse response = handleRequest(request, m);
                    if (response != null) responseBatch.add(response);
                } else {
                    responseBatch.add(JsonRpcDefaultError.invalidRequest());
                }
            }
            m.send(responseBatch);

        } else {
            //error
            m.send(JsonRpcDefaultError.invalidRequest());
        }

    }

    JsonRpcResponse handleRequest(JsonRpcRequest request, JsonRpcManager manager) {
        JsonRpcResponse response=null;
        if (filterRequest(request, manager, response))
            return response;

        if (verbose)
            System.out.println("connectionThread: method=\"" + request.getMethod() + "\"\trequest=" + request.toString());


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
                return JsonRpcDefaultError.methodNotFound(request.getID());
            }

        }
        return null;
    }


    class LocalConnection implements IConnection {

        String head;
        boolean unset = true;
        LocalConnection other = null;
        Queue<String> queue = new ArrayDeque<>();

        public LocalConnection() {
        }

        public LocalConnection(LocalConnection other) {
            this.other = other;
            other.other = this;
        }

        @Override
        public String read() {
            while (queue.isEmpty()) ;
            return queue.peek();
        }

        @Override
        public String read(long milliseconds) throws TimeoutException {
            if (queue.isEmpty()) throw new TimeoutException("");
            return queue.peek();
        }

        @Override
        public void consume() {
            queue.poll();
        }

        @Override
        public void send(String msg) {
            other.queue.add(msg);
        }

        @Override
        public void close() {

        }

    }

    class DeleterService implements IServiceMethod {
        Broker broker;

        public DeleterService(Broker broker) {
            this.broker = broker;
        }

        @Override
        public JsonRpcResponse run(JsonRpcRequest request) {
            JsonRpcResponse response = null;
            try {
                String name = request.getParams().getAsJsonObject().get("method").getAsString();
                for (ServiceMetadata s : services) {
                    if (s.getMethodName().equals(name)) {
                        services.remove(s);
                        servers.remove(name);
                        return null;
                    }
                }
            } catch (IllegalArgumentException e) {
                response = JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
                System.err.println("DeleterService: Wrong JSON-RPC Request received, a JSON-RPC Error is returned to requester");
            } finally {
                System.out.println("DeleterService: service deleted");
                return null;
            }
        }
    }

    class ListProviderService implements IServiceMethod {
        Broker broker;

        public ListProviderService(Broker broker) {
            this.broker = broker;
        }

        @Override
        public JsonRpcResponse run(JsonRpcRequest request) {
            JsonRpcResponse response = null;
            try {
                JsonObject j = request.getParams().getAsJsonObject();
                List<ServiceMetadata> list;

                if (j == null) list = getServicesList();
                else {
                    SearchStrategy searchStrategy = SearchStrategy.fromJson(j.toString());
                    if (searchStrategy == null) {
                        return JsonRpcResponse.error(JsonRpcCustomError.wrongSerchStrategy(), request.getID());
                    }
                    list = getServicesList(searchStrategy);
                }

                JsonArray result = new JsonArray();
                for (ServiceMetadata s : list) {
                    result.add(s.toJson());
                }

                if (broker.verbose) System.out.println("generated list (result):" + result.toString());
                if (broker.verbose) System.out.println("id:" + request.getID());
               return new JsonRpcResponse(result, request.getID());
            } catch (IllegalArgumentException e) {
                System.err.println("ListProviderService: Wrong JSON-RPC Request received, a JSON-RPC Error is returned to requester");
                return JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
            }
        }
    }

    Broker(IConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        brokerServices.put("getServicesList", new ListProviderService(this));
        brokerServices.put("deleteService", new DeleterService(this));
    }

    void close() {
        isClosed = true;
    }

    public final boolean verbose = true;

    void start() {
        isClosed = false;
        while (!isClosed) {

            if (verbose) System.out.println("Broker waiting for incoming connection...");

            IConnection c = connectionManager.acceptConnection();

            JsonRpcManager j = new JsonRpcManager(c);


            if (verbose) System.out.println("Broker handling the request");

            Thread t1 = new Thread(() -> {
                try {
                    connectionThread(j);
                    if (verbose) System.out.println("Broker handled the request");
                } catch (Exception e) {
                    // handle: log or throw in a wrapped RuntimeException
                    throw new RuntimeException("InterruptedException caught in lambda", e);
                }
            });
            t1.start();
        }
    }
}
