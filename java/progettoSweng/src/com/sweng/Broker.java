package com.sweng;

import java.util.*;

import com.jsonrpc.*;
import com.google.gson.*;
import javafx.util.Pair;


public class Broker extends Thread {

    public final boolean verbose = true;

    private IConnectionManager connectionManager;

    private Map<String, IServiceMethod> brokerServices = new HashMap<>();

    private Map<String, JsonRpcManager> servers = new HashMap<>();
    private List<ServiceMetadata> services = new LinkedList<>();

    /**
     * Generate a method name to identify unequivocally a Service.
     *
     * @param hint : if a service with this name already exists, it's added a suffix with the version number
     *             example:
     *             sum -> sum@1 -> sum@2 -> sum@3 ...
     * @return the generated name
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

    /**
     * This "service" is hard-coded because we need to have the reference to the connection manager
     *
     * @param request
     * @param manager
     * @return
     */
    JsonRpcResponse registerService(JsonRpcRequest request, JsonRpcManager manager) {
        JsonObject params = request.getParams().getAsJsonObject();

        if (params == null) throw new RuntimeException("failed to register a service: title not found");

        String name = params.get("method").getAsString();
        if (name.isEmpty()) throw new RuntimeException("failed to register a service: title not found");


        name = generateMethodName(name);

        Logger.log("registerService: generated name = " + name);

        ServiceMetadata serviceMetadata = ServiceMetadata.fromJson(request.getParams().getAsJsonObject());
        serviceMetadata.setMethodName(name);
        servers.put(name, manager);
        services.add(serviceMetadata);

        Logger.log("registerService: service registered");

        JsonObject result = new JsonObject();
        result.addProperty("serviceRegistered", true);
        result.addProperty("method", name);

        return new JsonRpcResponse(result, request.getID());
    }

    /**
     * @return a list with all the stored services
     */
    List<ServiceMetadata> getServicesList() {
        return services;
    }

    /**
     * @param searchStrategy
     * @return a list with all the stored services that satisfy the filter specified by searchStrategy
     */
    List<ServiceMetadata> getServicesList(SearchStrategy searchStrategy) {
        return searchStrategy.filterList(services);
    }

    /**
     * This method filter the request direct to the broker itself
     *
     * @param request
     * @param manager
     * @return a pair containing a boolean indicating if the request has been already handled, and a response.
     * The response is null if the request is a notification.
     */
    Pair<Boolean, JsonRpcResponse> filterRequest(JsonRpcRequest request, JsonRpcManager manager) {
        IServiceMethod service = brokerServices.get(request.getMethod());

        if (service != null) {
            return new Pair<>(true, service.run(request));
        }

        if (request.getMethod().equals("registerService")) {
            Logger.log("registerService request");
            return new Pair<>(true, registerService(request, manager));
        }

        return new Pair<>(false, null);//the request has to be forwarded
    }


    /**
     * @param manager
     */
    void handleConnection(JsonRpcManager manager) {

        Logger.log("connectionThread begin");

        JsonRpcMessage r = null;
        try {
            r = manager.listenRequest(1000);
        } catch (ParseException e) {
            //e.printStackTrace();
            manager.send(JsonRpcDefaultError.parseError());
            return;
        } catch (TimeoutException e) {

        }

        if (r instanceof JsonRpcRequest) {

            JsonRpcResponse response = handleRequest((JsonRpcRequest) r, manager);
            if (response != null) manager.send(response);

        } else if (r instanceof JsonRpcBatchRequest) {
            JsonRpcBatchRequest requestBatch = (JsonRpcBatchRequest) r;
            JsonRpcBatchResponse responseBatch = new JsonRpcBatchResponse();

            for (JsonRpcRequest request : requestBatch.get()) {
                if (request.isValid()) {
                    JsonRpcResponse response = handleRequest(request, manager);
                    if (response != null) responseBatch.add(response);
                } else {
                    responseBatch.add(JsonRpcDefaultError.invalidRequest());
                }
            }
            manager.send(responseBatch);

        } else {
            //error
            manager.send(JsonRpcDefaultError.invalidRequest());
        }
    }

    JsonRpcResponse handleRequest(JsonRpcRequest request, JsonRpcManager manager) {
        Pair<Boolean, JsonRpcResponse> filtered = filterRequest(request, manager);
        if (filtered.getKey())
            return filtered.getValue();


        if (verbose)
            System.out.println("connectionThread: method=\"" + request.getMethod() + "\"\trequest=" + request.toString());


        JsonRpcManager server = servers.get(request.getMethod());

        if (server != null) {
            server.send(request);

            if (request.isNotification()) return null; // if its a notification the broker simply forward it

            JsonRpcMessage res = null;
            try {
                res = server.listenResponse(1000);
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            manager.send(res);
        } else {
            return JsonRpcDefaultError.methodNotFound(request.getID());
        }


        return null;
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

    @Override
    public void run() {
        while (!this.isInterrupted()) {

            Logger.log("Broker waiting for incoming connection...");

            IConnection c = connectionManager.acceptConnection();

            JsonRpcManager j = new JsonRpcManager(c);


            Logger.log("Broker handling the request");

            Thread t1 = new Thread(() -> {
                try {
                    handleConnection(j);
                    Logger.log("Broker handled the request");
                } catch (Exception e) {
                    // handle: log or throw in a wrapped RuntimeException
                    throw new RuntimeException("InterruptedException caught in lambda", e);
                }
            });
            t1.start();
        }
    }
}
