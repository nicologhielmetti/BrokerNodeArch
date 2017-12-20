package com.sweng;

import com.jsonrpc.IConnection;
import javafx.util.Pair;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Poller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ZeroMQConnectionManager implements IConnectionManager {

    private Context context;
    private Socket frontend;
    private int index=0;

    private Map<String,Pair<Socket,Socket>> connections;
    private Queue<Pair<ZFrame,Pair<Socket,Socket>>> connections_queue;

    private int port;

    public ZeroMQConnectionManager(int port) {
        context = ZMQ.context(1);
        this.port = port;

        connections = new HashMap<>();
        connections_queue=new LinkedList<>();

        frontend = context.socket(ZMQ.ROUTER);
        frontend.bind("tcp://*:"+port);
    }

    void pollEvents() {

        Poller poller=context.poller(1);

        poller.register(frontend, Poller.POLLIN);
        if (poller.poll() < 0)
            return;

        if(poller.pollin(0)) {
            //First frame is identity
            ZMsg orig = ZMsg.recvMsg(frontend);
            ZMsg msg = orig.duplicate();

            //System.out.println("received : " + msg.toString());

            ZFrame identity = msg.pop();
            Pair<Socket, Socket> sockets = connections.get(identity.toString());

            if (sockets == null) {
                sockets = new Pair<>(context.socket(ZMQ.DEALER), context.socket(ZMQ.DEALER));

                sockets.getValue().connect("inproc://local" + index);
                sockets.getKey().bind("inproc://local" + index++);


                connections.put(identity.toString(), sockets);
                connections_queue.add(new Pair<>(identity, sockets));
            }

            //  Second frame is empty
            //String empty = msg.popString();
            //assert (empty.length() == 0);

            //  Third frame is READY or else a client reply address
            String request = msg.popString();


            System.out.println("ZeroMQConnectionManager received :" + identity + ";" + request);

            sockets.getKey().send(request);
        }
    }

    @Override
    public IConnection acceptConnection() {
        while(true) {
            if (connections_queue.isEmpty()) {
                pollEvents();
            }else{
                Pair<ZFrame, Pair<Socket, Socket>> connection=connections_queue.poll();
                return new ZeroMQConnection(connection.getValue().getValue(),frontend,connection.getKey(),context);
            }
        }
    }
}
