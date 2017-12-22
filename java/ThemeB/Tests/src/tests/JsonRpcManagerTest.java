package tests;

import com.google.gson.JsonArray;
import connectioninterfaces.IConnectionFactory;
import connectioninterfaces.IConnectionManager;
import connectioninterfaces.TimeoutException;
import jsonrpclibrary.*;
import zeromqimplementation.ZeroMQConnectionFactory;
import zeromqimplementation.ZeroMQConnectionManager;

import static org.junit.Assert.*;

public class JsonRpcManagerTest {
    private static JsonRpcManager managerReceiver, managerSender;
    private static IConnectionFactory connectionFactory;
    private static IConnectionManager connectionManager;

    @org.junit.Before
    public void setUp() throws Exception {
        if(connectionFactory == null && connectionManager == null) {
            connectionManager = new ZeroMQConnectionManager(6789);
            connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6789");
            managerSender = new JsonRpcManager(connectionFactory.createConnection());
        }
    }

    @org.junit.Test
    public void listenRequestSimpleRequest() throws Exception {
        JsonArray array = new JsonArray();
        array.add(42);
        array.add(23);
        managerSender.send(new JsonRpcRequest("subtract",array, new ID(1)));
        managerReceiver = new JsonRpcManager(connectionManager.acceptConnection());
        JsonRpcMessage message = managerReceiver.listenRequest();
        assertFalse(message.isBatch());
        JsonRpcRequest request = (JsonRpcRequest) message;
        assertEquals("{\"jsonrpc\":\"2.0\",\"method\":\"subtract\",\"params\":[42,23],\"id\":1}", request.toString());
    }

    @org.junit.Test
    public void listenRequestEmptyBatch() throws Exception {

    }


    @org.junit.Test
    public void listenResponse() throws Exception {
    }

    @org.junit.Test
    public void listenResponse1() throws Exception {
    }

    @org.junit.Test
    public void send() throws Exception {
    }

}