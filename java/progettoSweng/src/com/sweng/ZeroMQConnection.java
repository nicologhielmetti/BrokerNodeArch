package com.sweng;

import com.jsonrpc.IConnection;
import com.jsonrpc.TimeoutException;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class ZeroMQConnection implements IConnection {

    Socket socket, sender;
    ZFrame identity;

    String head;
    boolean unset = true;
    ZMQ.Context context;


    ZeroMQConnection(Socket socket, ZMQ.Context context) {
        this.socket = socket;
        this.context = context;
    }

    ZeroMQConnection(Socket receiver, Socket sender, ZFrame identity, ZMQ.Context context) {
        this.context = context;
        this.socket = receiver;
        this.sender = sender;
        this.identity = identity;
    }


    @Override
    public void finalize() {
        close();
    }

    public void close() {
        socket.close();
    }

    @Override
    public String read() {
        if (unset) {
            ZMsg msg = ZMsg.recvMsg(socket);
            do {
                head = msg.popString();
            } while (!msg.isEmpty());
            System.out.println("ZeroMQConnection received : \"" + head + "\"");
            unset = false;
        }
        return head;
    }

    @Override
    public String read(long milliseconds) throws TimeoutException{
        if (unset) {
            ZMQ.Poller poller = context.poller(1);

            poller.register(socket, ZMQ.Poller.POLLIN);
            if (poller.poll(milliseconds) < 0)
                throw new TimeoutException("");

            ZMsg msg = ZMsg.recvMsg(socket);
            do {
                head = msg.popString();
            } while (!msg.isEmpty());
            System.out.println("ZeroMQConnection received : \"" + head + "\"");
            unset = false;
        }
        return head;
    }

    @Override
    public void consume() {
        if (unset) {
            head = socket.recvStr();
        }
        unset = true;
    }

    @Override
    public void send(String msg) {
        System.out.println("Sending : " + msg);
        if (sender == null) {
            ZMsg z = new ZMsg();
            z.push(msg);

            z.send(socket);
        } else {
            ZMsg z = new ZMsg();
            z.push(msg);
            z.push(identity.duplicate());
            z.send(sender);
        }
    }

}
