package com.sweng;
import com.jsonrpc.IConnection;
import org.zeromq.ZMQ;

public class ZeroMQConnectionFactory implements IConnectionFactory {
    ZMQ.Context context;
    String address;

    ZeroMQConnectionFactory(String address){ //eg:"tcp://localhost:5555"
        context = ZMQ.context(1);
        this.address=address;
    }


    @Override
    public void finalize() {
        context.term();
    }

    //todo handle ObjectPool
    public IConnection createConnection() {
        ZMQ.Socket socket = context.socket(ZMQ.DEALER);
        socket.connect(address);
        return new ZeroMQConnection(socket);
    }

}
