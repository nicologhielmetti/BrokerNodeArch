package com.sweng;

import com.jsonrpc.IConnection;
import org.zeromq.ZMQ;

public class ZeroMQConnection implements IConnection {

    ZMQ.Socket socket,sender;
    String identity;

    String head;
    boolean unset=true;


    ZeroMQConnection(ZMQ.Socket socket){
        this.socket=socket;
    }

    ZeroMQConnection(ZMQ.Socket receiver, ZMQ.Socket sender,String identity){
        this.socket=receiver;
        this.sender=sender;
        this.identity=identity;
    }


    @Override
    public void finalize(){
        close();
    }

    public void close(){
        socket.close();
    }


    @Override
    public String read() {
        if(unset){
            //String identity=socket.recvStr();
            do{
                head=socket.recvStr();
                System.out.println("ZeroMQConnection head = "+head);
            }while(socket.hasReceiveMore());
            //head=socket.recvStr();
            System.out.println("ZeroMQConnection received : \""+head+"\"");

            unset=false;
        }
        return head;
    }

    @Override
    public void consume(){
        if(unset){
             head=socket.recvStr();
        }
        unset=true;
    }
    @Override
    public void send(String msg) {
        System.out.println("Sending : "+msg);
        if(sender==null){
            socket.sendMore("");
            socket.send(msg);
        }else{
            sender.sendMore(identity);
            sender.sendMore("");
            sender.send(msg);
        }
    }

}
