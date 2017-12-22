package tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import connectioninterfaces.IConnection;
import jsonrpclibrary.Error;
import jsonrpclibrary.*;
import org.junit.Test;
import zeromqimplementation.ZeroMQConnectionFactory;
import zeromqimplementation.ZeroMQConnectionManager;

import static org.junit.Assert.*;

public class JsonRpcManagerTest {
    

    @Test (timeout = 1000,expected = ParseException.class)
    public void parseException() throws Exception {
        ZeroMQConnectionManager connectionManager = new ZeroMQConnectionManager(6800);
        ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6800");
        IConnection connection = connectionFactory.createConnection();


        connection.send("blablaaasdjlasjdkjaskldak");

        JsonRpcManager managerReceiver = new JsonRpcManager(connectionManager.acceptConnection());
        JsonRpcMessage message = managerReceiver.listenRequest();
    }


    @Test (timeout = 1000)
    public void listenRequestSimpleRequest() throws Exception {
        ZeroMQConnectionManager connectionManager = new ZeroMQConnectionManager(6793);
        ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6793");
        JsonRpcManager managerSender = new JsonRpcManager(connectionFactory.createConnection());


        JsonArray array = new JsonArray();
        array.add(42);
        array.add(23);
        managerSender.send(new JsonRpcRequest("subtract",array, new ID(1)));
        JsonRpcManager managerReceiver = new JsonRpcManager(connectionManager.acceptConnection());
        JsonRpcMessage message = managerReceiver.listenRequest();
        assertFalse(message.isBatch());
        JsonRpcRequest request = (JsonRpcRequest) message;
        assertEquals("{\"jsonrpc\":\"2.0\",\"method\":\"subtract\",\"params\":[42,23],\"id\":1}", request.toString());
    }



    @Test
    public void JsonRpcResponse() throws Exception{
        JsonRpcResponse response=new JsonRpcResponse(new JsonPrimitive(2),new ID(2));

        assertNull(JsonRpcBatchRequest.fromJson(response.toJson()));// it can be a response full of invalid requests
        assertNull(JsonRpcBatchResponse.fromJson(response.toJson()));
        assertNull(JsonRpcRequest.fromJson(response.toJson()));
        assertNotNull(JsonRpcResponse.fromJson(response.toJson()));

        response= JsonRpcResponse.error(new Error(42,"lol"),new ID(1337));
        assertNull(JsonRpcBatchRequest.fromJson(response.toJson()));// it can be a response full of invalid requests
        assertNull(JsonRpcBatchResponse.fromJson(response.toJson()));
        assertNull(JsonRpcRequest.fromJson(response.toJson()));
        assertNotNull(JsonRpcResponse.fromJson(response.toJson()));
    }
    
    @Test
    public void JsonRpcRequest() throws Exception{
        JsonRpcRequest request=new JsonRpcRequest("sum",new JsonPrimitive(2),new ID(2));

        assertNull(JsonRpcBatchRequest.fromJson(request.toJson()));// it can be a request full of invalid requests
        assertNull(JsonRpcBatchResponse.fromJson(request.toJson()));
        assertNotNull(JsonRpcRequest.fromJson(request.toJson()));
        assertNull(JsonRpcResponse.fromJson(request.toJson()));

        request= JsonRpcRequest.notification("WHATSAAAAAPPP?",null); 
        assertNull(JsonRpcBatchRequest.fromJson(request.toJson()));// it can be a request full of invalid requests
        assertNull(JsonRpcBatchResponse.fromJson(request.toJson()));
        assertNotNull(JsonRpcRequest.fromJson(request.toJson()));
        assertNull(JsonRpcResponse.fromJson(request.toJson()));
    }
    
    
    @Test
    public void JsonRpcBatchResponse() throws Exception{
        JsonRpcBatchResponse batch=new JsonRpcBatchResponse();
        batch.add(new JsonRpcResponse(new JsonPrimitive(2),new ID(2)));
        batch.add(new JsonRpcResponse(new JsonPrimitive(3),new ID(3)));

        System.out.println(batch.toJson());
        assertNotNull(JsonRpcBatchRequest.fromJson(batch.toJson()));// it can be a batch full of invalid requests
        assertNotNull(JsonRpcBatchResponse.fromJson(batch.toJson()));
        assertNull(JsonRpcRequest.fromJson(batch.toJson()));
        assertNull(JsonRpcResponse.fromJson(batch.toJson()));

        batch.add(new JsonRpcResponse(new JsonPrimitive("4"),new ID("4")));
        System.out.println(batch.toJson());
        assertNotNull(JsonRpcBatchRequest.fromJson(batch.toJson()));// it can be a batch full of invalid requests
        assertNotNull(JsonRpcBatchResponse.fromJson(batch.toJson()));
        assertNull(JsonRpcRequest.fromJson(batch.toJson()));
        assertNull(JsonRpcResponse.fromJson(batch.toJson()));
    }


    @Test
    public void JsonRpcBatchRequest() throws Exception{
        JsonRpcBatchRequest batch=new JsonRpcBatchRequest();
        batch.add(new JsonRpcRequest("sum",new JsonPrimitive(2),new ID(2)));
        batch.add(new JsonRpcRequest("nada",null,new ID(3)));

        System.out.println(batch.toJson());
        assertNotNull(JsonRpcBatchRequest.fromJson(batch.toJson()));
        assertNull(JsonRpcBatchResponse.fromJson(batch.toJson()));
        assertNull(JsonRpcRequest.fromJson(batch.toJson()));
        assertNull(JsonRpcResponse.fromJson(batch.toJson()));

        batch.add(new JsonRpcRequest("foo",new JsonPrimitive("4"),new ID("4")));
        System.out.println(batch.toJson());
        assertNotNull(JsonRpcBatchRequest.fromJson(batch.toJson()));
        assertNull(JsonRpcBatchResponse.fromJson(batch.toJson()));
        assertNull(JsonRpcRequest.fromJson(batch.toJson()));
        assertNull(JsonRpcResponse.fromJson(batch.toJson()));
    }

    @Test
    public void ID(){
        ID a=new ID(1);
        ID b=new ID(1);
        assertTrue(a.equals(b));
        assertTrue(a.equals(1));
        assertTrue(!a.equals("1"));
        assertTrue(!a.equals(null));

        a.set(2);
        assertTrue(!a.equals(b));
        assertTrue(!a.equals("1"));
        assertTrue(!a.equals(1));
        assertTrue(!a.equals(null));

        a.set("1");
        assertTrue(!a.equals(b));
        assertTrue(a.equals("1"));
        assertTrue(!a.equals(1));
        assertTrue(!a.equals(null));

        b.set("1");
        assertTrue(a.equals(b));

        a.set("2");
        assertTrue(!a.equals(b));
        assertTrue(!a.equals("1"));
        assertTrue(!a.equals(1));
        assertTrue(!a.equals(null));

        a.setNull();
        assertTrue(!a.equals(b));
        assertTrue(!a.equals("1"));
        assertTrue(!a.equals(1));
        assertTrue(a.equals(null));

        b.setNull();
        assertTrue(a.equals(b));
        assertTrue(!a.equals("1"));
        assertTrue(!a.equals(1));
        assertTrue(a.equals(null));

        System.out.println("ID...ok!");
    }

}