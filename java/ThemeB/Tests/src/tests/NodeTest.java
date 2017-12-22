package tests;

import broker.Broker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.util.Pair;
import jsonrpclibrary.JsonRpcBatchResponse;
import jsonrpclibrary.JsonRpcResponse;
import node.Node;
import org.junit.Test;
import searchstrategy.OwnerSearchStrategy;
import service.IServiceMethod;
import service.JsonRpcCustomError;
import service.ServiceMetadata;
import zeromqimplementation.ZeroMQConnectionFactory;
import zeromqimplementation.ZeroMQConnectionManager;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeTest {
    private Node node;
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
        } catch (IllegalArgumentException e) {
            return JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
        }
    };

    private IServiceMethod power = request -> {
        try {
            JsonArray jsonArray = request.getParams().getAsJsonArray();
            if (jsonArray.size() <= 0) return new JsonRpcResponse(new JsonPrimitive(0), request.getID());
            double power = jsonArray.get(0).getAsDouble();
            for (int i = 0; i < jsonArray.size() - 1; i++)
                power = pow(power, jsonArray.get(i + 1).getAsDouble());
            return new JsonRpcResponse(new JsonPrimitive(power), request.getID());
        } catch (IllegalArgumentException e) {
            return JsonRpcResponse.error(JsonRpcCustomError.wrongParametersReceived(), request.getID());
        }
    };



    public void provideService() throws Exception {
        divideMetadata = new ServiceMetadata("divide", "NodeTester");
        divideMetadata.setDescription("Divide num1 / num2. Parameters format: num1:double,num2:double.");
        divideMetadata.setApplicationField("Math");

        powerMetadata = new ServiceMetadata("power", "NodeTester");
        divideMetadata.setDescription(
                "Get power of n numbers given as params (power = (((n1^(n2))^(n3))^(n4))^... . Parameters format: [n1, n2, ...].");
        divideMetadata.setApplicationField("Math");

        boolean isRunning = node.provideService(divideMetadata, divide) && node.provideService(powerMetadata, power);
        assertEquals(true, isRunning);
    }

    public void deleteService() throws Exception {
        provideService();
        node.deleteService("divide");
        ArrayList<String> runningServicesName = node.showRunningServices();
        for (String s : runningServicesName)
            assertEquals(true, !s.equals("divide"));
    }

    public void requestService() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("num1", 10);
        jsonObject.addProperty("num2", 100);
        JsonRpcResponse response = node.requestService("divide", jsonObject);
        assertEquals(true, Math.abs(response.getResult().getAsJsonObject().get("quotient").getAsDouble() - 0.1) <= 1e-15);
    }


    public void requestService_batch() throws Exception {
        ArrayList<Pair<String, JsonElement>> listOfServices = new ArrayList<>();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("num1", 12.789);
        jsonObject.addProperty("num2", 114.789991); // quotient = 0.11141215
        Pair<String, JsonElement> request1 = new Pair<>("divide", jsonObject);

        JsonArray array = new JsonArray();
        array.add(2);
        array.add(3);
        array.add(2);
        Pair<String, JsonElement> request2 = new Pair<>("power", array);

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

    public void requestServiceList() throws Exception {
        ArrayList<ServiceMetadata> metadata = node.requestServiceList(new OwnerSearchStrategy("NodeTester"));
        assertTrue(metadata.size() == 2);
        assertTrue(
                metadata.get(0).getMethodName().equals("divide") ^ metadata.get(1).getMethodName().equals("divide"));
        assertTrue(
                metadata.get(0).getMethodName().equals("power") ^ metadata.get(1).getMethodName().equals("power"));
    }


    @Test
    public void test() throws Exception {
        ZeroMQConnectionManager connectionManager = new ZeroMQConnectionManager(6801);
        ZeroMQConnectionFactory connectionFactory1 = new ZeroMQConnectionFactory("tcp://localhost:6801");
        node=new Node(connectionFactory1);

        Broker broker=new Broker(connectionManager);
        broker.start();


        provideService();

        requestServiceList();
        requestService_batch();
        requestService();
        requestService();
        requestService();
        requestService();

        deleteService();

    }
}