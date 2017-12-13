package com.sweng;

import com.jsonrpc.IConnection;
import javafx.util.Pair;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

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
    Map<ZFrame,Pair<Socket,Socket>> connections;
    Queue<Pair<ZFrame,Pair<Socket,Socket>>> connections_queue;

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
//        //First frame is identity
//        String identity = backend.recvStr();
//        Pair<Socket, Socket> sockets = connections.get(identity);
//
//        if (sockets == null) {
//            sockets = new Pair<>(context.socket(ZMQ.REQ),context.socket(ZMQ.REP));
//
//            sockets.getValue().connect("inproc://local"+ index );
//            sockets.getKey().bind("inproc://local"+ index++ );
//
//
//            connections.put(backend.recvStr(), sockets);
//            connections_queue.add(new Pair<>(identity,sockets));
//        }
//
//        //  Second frame is empty
//        String empty = backend.recvStr();
//        assert (empty.length() == 0);
//
//        //  Third frame is READY or else a client reply address
//        String request = backend.recvStr();
//
//
//
//        System.out.println("ZeroMQConnectionManager received :"+identity+";"+empty+";"+request);
//
////        backend.sendMore(identity);
////        backend.sendMore("");
////        backend.send("bella zio pietro!!11!");
//
//
//        sockets.getKey().send(request);


        //First frame is identity
        ZMsg orig=ZMsg.recvMsg(backend); //todo change backend to frontend
        ZMsg msg=orig.duplicate();

        System.out.println("received : "+msg.toString());

        ZFrame identity =msg.pop();
        Pair<Socket, Socket> sockets = connections.get(identity);

        if (sockets == null) {
            sockets = new Pair<>(context.socket(ZMQ.REQ),context.socket(ZMQ.REP));

            sockets.getValue().connect("inproc://local"+ index );
            sockets.getKey().bind("inproc://local"+ index++ );


            connections.put(identity, sockets);
            connections_queue.add(new Pair<>(identity,sockets));
        }

        //  Second frame is empty
//        String empty = msg.popString();
//        assert (empty.length() == 0);

        //  Third frame is READY or else a client reply address
        String request = msg.popString();



        System.out.println("ZeroMQConnectionManager received :"+identity+";"+request);

//        backend.sendMore(identity);
//        backend.sendMore("");
//        backend.send("bella zio pietro!!11!");

        //msg.clear();
        //msg.push("ciao");
        //msg.push(identity);
        //msg.send(backend);

        sockets.getKey().send(request);
    }

    @Override
    public IConnection acceptConnection() {
        while(true) {
            if (connections_queue.isEmpty()) {
                pollEvents();
            }else{
                Pair<ZFrame, Pair<Socket, Socket>> connection=connections_queue.poll();
                return new ZeroMQConnection(connection.getValue().getValue(),backend,connection.getKey());
            }
        }
    }
}
