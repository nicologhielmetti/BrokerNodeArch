package com.sweng;

import com.jsonrpc.IConnection;
import org.zeromq.ZMQ;

public class ZeroMQConnection implements IConnection {

    ZMQ.Socket socket;
    String head;
    boolean unset=true;

    ZeroMQConnection(ZMQ.Socket socket){
        this.socket=socket;
    }
    @Override
    public void finalize(){
        close();
    }

    public void close(){
        socket.close();
    }



    public String read() {
        if(unset){
            head=socket.recvStr();
            unset=false;
        }
        return head;
    }

    public void consume(){
        if(unset){
             head=socket.recvStr();
        }
        unset=true;
    }
    public void send(String msg) {
        socket.send(msg);
    }

}
