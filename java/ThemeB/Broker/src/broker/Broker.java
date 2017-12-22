package broker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.*;
import connectioninterfaces.IConnection;
import connectioninterfaces.IConnectionManager;
import connectioninterfaces.TimeoutException;
import javafx.util.Pair;
import jsonrpclibrary.*;
import logger.Logger;
import searchstrategy.SearchStrategy;
import service.IServiceMethod;
import service.JsonRpcCustomError;
import service.ServiceMetadata;


public class Broker extends Thread {

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
    private String generateMethodName(String hint) {
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
     * @param request : the request must have as parameters a JsonObject containing a ServiceMetadata
     * @param manager : the JsonRpcManager which manages the request
     * @return : a JsonRpcResponse containing the outcome of the request
     */
    private JsonRpcResponse registerService(JsonRpcRequest request, JsonRpcManager manager) {
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
    private List<ServiceMetadata> getServicesList() {
        return services;
    }

    /**
     * @param searchStrategy : an implementation of SearchStrategy
     * @return a list with all the stored services that satisfy the filter specified by searchStrategy
     */
    private List<ServiceMetadata> getServicesList(SearchStrategy searchStrategy) {
        return searchStrategy.filterList(services);
    }

    /**
     * This method filter the request direct to the broker itself
     *
     * @param request : the request that has to be processed
     * @param manager : the manager handling the request
     * @return a pair containing a boolean indicating if the request has been already handled, and a response.
     * The response is null if the request is a notification.
     */
    private Pair<Boolean, JsonRpcResponse> filterRequest(JsonRpcRequest request, JsonRpcManager manager) {
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
     * @param manager the manager handling the connection
     */
    private void handleConnection(JsonRpcManager manager) {

        JsonRpcMessage r;
        try {
            r = manager.listenRequest(1000);
        } catch (ParseException e) {
            Logger.error("Parse exception : received an invalid json-rpc message");
            manager.send(JsonRpcResponse.error(JsonRpcDefaultError.parseError(),null));
            return;
        } catch (TimeoutException e) {
            //should never happen ( a connection is created when a message is received. So, where is the message?)
            Logger.error("Timeout exception : cannot read the first message of a connection :" +
                    " there is something wrong in the IConnection/IConnectionManager implementations ");
            return;
        }

        if (r instanceof JsonRpcRequest) {
            JsonRpcRequest request=(JsonRpcRequest) r;
            JsonRpcResponse response = handleRequest(request, manager);
            if (response != null) manager.send(response);
            if(request.getMethod().equals("registerService"))return;
        } else if (r instanceof JsonRpcBatchRequest) {
            JsonRpcBatchRequest requestBatch = (JsonRpcBatchRequest) r;
            JsonRpcBatchResponse responseBatch = new JsonRpcBatchResponse();

            for (JsonRpcRequest request : requestBatch.get()) {
                if (request.isValid()) {
                    JsonRpcResponse response = handleRequest(request, manager);
                    if (response != null) responseBatch.add(response);
                } else {
                    responseBatch.add(JsonRpcResponse.error(JsonRpcDefaultError.invalidRequest(),null));
                }
            }
            manager.send(responseBatch);

        } else {
            //error
            manager.send(JsonRpcResponse.error(JsonRpcDefaultError.invalidRequest(),null));
        }

        //the request is handled : we can now free the connection
        manager.getConnection().close();
    }

    /**
     * @param request the JsonRpcRequest object that has to be processed.
     * @param manager the manager handling the request.
     * @return the response generated from the request.
     * If the request is a notification, null is returned.
     */
    private JsonRpcResponse handleRequest(JsonRpcRequest request, JsonRpcManager manager) {
        //check if it's a request to a broker internal service
        Pair<Boolean, JsonRpcResponse> filtered = filterRequest(request, manager);
        if (filtered.getKey())
            return filtered.getValue();


        Logger.log("handleRequest: method=\"" + request.getMethod() + "\"\trequest=" + request.toString());

        //check if the method requested is registered
        JsonRpcManager server = servers.get(request.getMethod());

        if (server != null) {
            server.send(request);

            if (request.isNotification()) return null; // if its a notification the broker simply forward it

            JsonRpcMessage res;
            try {
                res = server.listenResponse(1000);
            } catch (ParseException e) {
                Logger.error("ParseException : received an invalid json-rpc message " +
                        "( listening for a response from \"" + request.getMethod() + "\" server)");
                res = JsonRpcResponse.error(JsonRpcDefaultError.internalError(),request.getID());
            } catch (TimeoutException e) {
                Logger.error("TimeoutException :" +
                        " the \"" + request.getMethod() + "\" server did not respond in time( within 1 second ).");
                res = JsonRpcResponse.error(JsonRpcCustomError.connectionTimeout(),request.getID());
            }
            if (!(res instanceof JsonRpcResponse)) {
                Logger.error("\"" + request.getMethod() + "\" server " +
                        "responded with a batch response to a non-batch request");
                res = JsonRpcResponse.error(JsonRpcDefaultError.internalError(),request.getID());
            }
            return (JsonRpcResponse) res;
        } else {
            return JsonRpcResponse.error(JsonRpcDefaultError.methodNotFound(),request.getID());
        }
    }


    class DeleterService implements IServiceMethod {
        Broker broker;

        DeleterService(Broker broker) {
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
                        response = null;
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
                response = JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
                Logger.error("DeleterService:" +
                        " Wrong JSON-RPC Request received, a JSON-RPC Error is returned to requester");
            }
            Logger.log("DeleterService: service deleted");
            return response;

        }
    }

    class ListProviderService implements IServiceMethod {
        Broker broker;

        ListProviderService(Broker broker) {
            this.broker = broker;
        }

        @Override
        public JsonRpcResponse run(JsonRpcRequest request) {
            try {
                JsonObject j = request.getParams()!=null ? request.getParams().getAsJsonObject() : null;
                List<ServiceMetadata> list;

                if (j == null) list = getServicesList();
                else {
                    SearchStrategy searchStrategy = SearchStrategy.fromJson(j.toString());
                    if (searchStrategy == null) {
                        return JsonRpcResponse.error(JsonRpcCustomError.wrongSearchStrategy(), request.getID());
                    }
                    list = getServicesList(searchStrategy);
                }

                JsonArray result = new JsonArray();
                for (ServiceMetadata s : list) {
                    result.add(s.toJson());
                }

                Logger.log("generated list (result):" + result.toString());
                Logger.log("id:" + request.getID());
                return new JsonRpcResponse(result, request.getID());
            } catch (IllegalArgumentException e) {
                Logger.error("ListProviderService: Wrong JSON-RPC Request received, a JSON-RPC Error is returned to requester");
                return JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
            }
        }
    }

    public Broker(IConnectionManager connectionManager) {
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
