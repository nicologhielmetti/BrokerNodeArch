package tests;

import connectioninterfaces.IConnection;
import connectioninterfaces.IConnectionFactory;
import connectioninterfaces.IConnectionManager;
import connectioninterfaces.TimeoutException;
import jsonrpclibrary.JsonRpcManager;
import org.junit.Test;
import zeromqimplementation.ZeroMQConnectionFactory;
import zeromqimplementation.ZeroMQConnectionManager;

import static org.junit.Assert.*;

public class ConnectionTest {
    @Test
    public void send_receive() throws TimeoutException{
        IConnectionFactory connectionFactory;
        IConnectionManager connectionManager;


        connectionManager = new ZeroMQConnectionManager(6791);
        connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6791");
        IConnection sender=connectionFactory.createConnection();
        sender.send("hello!");

        IConnection receiver=connectionManager.acceptConnection();
        assertEquals("hello!",receiver.read());
        receiver.consume();

        receiver.send("hi!");
        assertEquals("hi!",sender.read());
        sender.consume();

    }

    @Test (timeout=1000,expected = TimeoutException.class)
    public void timeout() throws TimeoutException {
        IConnectionFactory connectionFactory;
        IConnectionManager connectionManager;


        connectionManager = new ZeroMQConnectionManager(6792);
        connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6792");
        IConnection sender=connectionFactory.createConnection();
        sender.send("hello there!");

        IConnection receiver=connectionManager.acceptConnection();
        assertEquals("hello there!",receiver.read());
        receiver.consume();

        receiver.read(200);
    }

    @Test(expected = TimeoutException.class,timeout = 500)
    public void timeout_at_start() throws Exception {
        ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6794");
        connectionFactory.createConnection().read(200);
    }


}
