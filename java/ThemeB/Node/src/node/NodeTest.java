package node;

import com.google.gson.JsonObject;
import jsonrpclibrary.JsonRpcRequest;
import jsonrpclibrary.JsonRpcResponse;
import service.IServiceMethod;
import service.JsonRpcCustomError;
import service.ServiceMetadata;
import zeromqimplementation.ZeroMQConnectionFactory;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class NodeTest {
    private Node node;
    private ServiceMetadata serviceMetadata;
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

    @org.junit.Test
    public void provideService() throws Exception {
        node = new Node(new ZeroMQConnectionFactory("tcp://localhost:6789"));
        serviceMetadata = new ServiceMetadata("divide","NodeTester");
        serviceMetadata.setDescription("Divide num1 / num2. Parameters format: num1:double,num2:double.");
        serviceMetadata.setApplicationField("Math");
        boolean isRunning = node.provideService(serviceMetadata, divide);
        assertEquals(isRunning,true);
    }

    @org.junit.Test
    public void deleteService() throws Exception {
        provideService();
        node.deleteService("divide");
        ArrayList<String> runninServicesName = node.showRunningServices();
        assertEquals(runninServicesName.isEmpty(), true);
    }

    @org.junit.Test
    public void requestService() throws Exception {

    }

    @org.junit.Test
    public void requestService1() throws Exception {

    }

    @org.junit.Test
    public void requestServiceList() throws Exception {

    }

}