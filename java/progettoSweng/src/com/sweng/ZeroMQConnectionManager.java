package com.sweng;

import com.jsonrpc.IConnection;
import javafx.util.Pair;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ZeroMQConnectionManager implements IConnectionManager {

    Context context;
    Socket frontend;
    Socket backend;
    int index=0;

    //Semaphore semaphore=new Semaphore();
    Map<String,Pair<Socket,Socket>> connections;
    Queue<Pair<String,Pair<Socket,Socket>>> connections_queue;

    int port;

    ZeroMQConnectionManager(int port) {
        context = ZMQ.context(1);
        this.port = port;

        connections = new HashMap<>();
        connections_queue=new LinkedList<>();

        //Socket frontend = context.socket(ZMQ.ROUTER);
        backend = context.socket(ZMQ.ROUTER);
        //frontend.bind("ipc://frontend.ipc");
        backend.bind("tcp://*:"+port);
    }

    public void pollEvents() {
        //First frame is identity
        String identity = backend.recvStr();
        Pair<Socket, Socket> sockets = connections.get(identity);

        if (sockets == null) {
            sockets = new Pair<>(context.socket(ZMQ.REQ),context.socket(ZMQ.REP));

            sockets.getValue().connect("ipc://local"+ index +".ipc");
            sockets.getKey().connect("ipc://local"+ index++ +".ipc");


            connections.put(backend.recvStr(), sockets);
            connections_queue.add(new Pair<>(identity,sockets));
        }

        //  Second frame is empty
        String empty = backend.recvStr();
        assert (empty.length() == 0);

        //  Third frame is READY or else a client reply address
        String request = backend.recvStr();


        System.out.println("ZeroMQConnectionManager received :"+identity+";"+empty+";"+request);
        sockets.getKey().send(request);
    }

    @Override
    public IConnection acceptConnection() {
        while(true) {
            if (connections_queue.isEmpty()) {
                pollEvents();
            }else{
                Pair<String, Pair<Socket, Socket>> connection=connections_queue.poll();
                return new ZeroMQConnection(connection.getValue().getValue(),backend,connection.getKey());
            }
        }
    }
}
