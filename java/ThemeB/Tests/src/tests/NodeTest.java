package tests;

import broker.Broker;
import broker.DummyBroker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import connectioninterfaces.IConnectionFactory;
import connectioninterfaces.IConnectionManager;
import javafx.util.Pair;
import jsonrpclibrary.ID;
import jsonrpclibrary.JsonRpcBatchResponse;
import jsonrpclibrary.JsonRpcRequest;
import jsonrpclibrary.JsonRpcResponse;
import node.Node;
import searchstrategy.OwnerSearchStrategy;
import service.IServiceMethod;
import service.JsonRpcCustomError;
import service.ServiceMetadata;
import zeromqimplementation.ZeroMQConnectionFactory;
import zeromqimplementation.ZeroMQConnectionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeTest {
    private Node node;
    private static Broker broker;
    private static IConnectionFactory connectionFactory;
    private static IConnectionManager connectionManager;
    private ServiceMetadata divideMetadata, powerMetadata;
    private IServiceMethod divide = request -> {
        try {
            JsonObject jsonObject = request.getParams().getAsJsonObject();
            double num1 = jsonObject.get("num1").getAsDouble();
            double num2 = jsonObject.get("num2").getAsDouble();
            double quotient = num1 / num2;
            jsonObject = new JsonObject();
            jsonObject.addProperty("quotient", quotient);
            return new JsonRpcResponse(jsonObject, request.getID());
        } catch (IllegalArgumentException e){
            return JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
        }
    };

    private  IServiceMethod power = request -> {
        try {
            JsonArray jsonArray = request.getParams().getAsJsonArray();
            if(jsonArray.size() <= 0) return new JsonRpcResponse(new JsonPrimitive(0),request.getID());
            double power = jsonArray.get(0).getAsDouble();
            for(int i = 0; i < jsonArray.size() - 1; i++)
                power = pow(power, jsonArray.get(i+1).getAsDouble());
            return new JsonRpcResponse(new JsonPrimitive(power), request.getID());
        } catch (IllegalArgumentException e){
            return JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
        }
    };

    @org.junit.Before
    public void setUp() throws Exception {
        if(connectionManager == null) {
            connectionManager = new ZeroMQConnectionManager(6789);
            broker = new Broker(connectionManager);
            broker.start();
        }
        if(connectionFactory == null) {
            connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6789");
            node = new Node(connectionFactory);
        }
    }

    @org.junit.Test
    public void provideService() throws Exception {
        node = new Node(connectionFactory);
        divideMetadata = new ServiceMetadata("divide","NodeTester");
        divideMetadata.setDescription("Divide num1 / num2. Parameters format: num1:double,num2:double.");
        divideMetadata.setApplicationField("Math");

        powerMetadata = new ServiceMetadata("power","NodeTester");
        divideMetadata.setDescription(
                "Get power of n numbers given as params (power = (((n1^(n2))^(n3))^(n4))^... . Parameters format: [n1, n2, ...].");
        divideMetadata.setApplicationField("Math");

        boolean isRunning = node.provideService(divideMetadata, divide) && node.provideService(powerMetadata,power);
        assertEquals(true, isRunning);
    }

    @org.junit.Test
    public void deleteService() throws Exception {
        provideService();
        node.deleteService("divide");
        ArrayList<String> runninServicesName = node.showRunningServices();
        for(String s : runninServicesName)
            assertEquals( true, !s.equals("divide"));
    }

    @org.junit.Test
    public void requestService() throws Exception {
        provideService();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("num1",10);
        jsonObject.addProperty("num2",100);
        JsonRpcResponse response = node.requestService("divide",jsonObject);
        assertEquals(true , Math.abs(response.getResult().getAsJsonObject().get("quotient").getAsDouble() - 0.1) <= 1e-15);
    }

    @org.junit.Test
    public void batch() throws Exception{
        JsonRpcBatchResponse batch=new JsonRpcBatchResponse();
        batch.add(new JsonRpcResponse(new JsonPrimitive(2),new ID(2)));
        batch.add(new JsonRpcResponse(new JsonPrimitive(3),new ID(3)));

        System.out.println(batch.toJson());
        assertTrue(JsonRpcBatchResponse.fromJson(batch.toJson())!=null);

        batch.add(new JsonRpcResponse(new JsonPrimitive("4"),new ID("4")));
        System.out.println(batch.toJson());
        assertTrue(JsonRpcBatchResponse.fromJson(batch.toJson())!=null);
    }

    @org.junit.Test
    public void requestService1() throws Exception {
        provideService();
        ArrayList<Pair<String, JsonElement>> listOfServices = new ArrayList<>();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("num1",12.789);
        jsonObject.addProperty("num2",114.789991); // quotient = 0.11141215
        Pair<String, JsonElement> request1 = new Pair<>("divide",jsonObject);

        JsonArray array = new JsonArray();
        array.add(2);
        array.add(3);
        array.add(2);
        Pair<String, JsonElement> request2 = new Pair<>("power",array);

        listOfServices.add(request1);
        listOfServices.add(request2);

        JsonRpcBatchResponse batchResponse = node.requestService(listOfServices);
        List<JsonRpcResponse> responses = batchResponse.get();
        assertTrue(responses.size() == 2);
        assertTrue(
                Math.abs(responses.get(0).getResult().getAsJsonObject().get("quotient").getAsDouble() - 0.11141215) <= 1e-5);
        assertTrue(
                Math.abs(responses.get(1).getResult().getAsJsonPrimitive().getAsDouble() - 64) <= 1e-15);
    }

    @org.junit.Test
    public void requestServiceList() throws Exception {
        provideService();
        ArrayList<ServiceMetadata> metadata = node.requestServiceList(new OwnerSearchStrategy("NodeTester"));
        assertTrue(metadata.size() == 2);
        assertTrue(
                metadata.get(0).getMethodName().equals("divide") ^ metadata.get(1).getMethodName().equals("divide"));
        assertTrue(
                metadata.get(0).getMethodName().equals("power") ^ metadata.get(1).getMethodName().equals("power"));
    }

}