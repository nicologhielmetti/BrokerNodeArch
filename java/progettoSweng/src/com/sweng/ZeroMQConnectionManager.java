package com.sweng;

import com.jsonrpc.IConnection;
import org.zeromq.ZMQ;

public class ZeroMQConnectionManager implements IConnectionManager {

    ZMQ.Context context;
    ZMQ.Socket socket;

    ZeroMQConnectionManager(){
        context = ZMQ.context(1);

        //socket=context.socket(ZMQ.PUB);

    }



    @Override
    public IConnection acceptConnection() {

    }
}
