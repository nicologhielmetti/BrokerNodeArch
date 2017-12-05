package com.jsonrpc;

import java.lang.String;

public interface IConnection {
    public String receive();
    public void send(String msg);
}
