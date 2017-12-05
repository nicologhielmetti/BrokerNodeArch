package com.sweng;

import com.jsonrpc.IConnection;

public class ZeroMQConnection implements IConnection {
    public String receive() { return null; }
    public void send(String msg) {}
}
