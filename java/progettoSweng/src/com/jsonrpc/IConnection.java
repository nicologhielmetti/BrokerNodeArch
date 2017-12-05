package com.jsonrpc;

import java.lang.String;

public interface IConnection {
    public String read();
    public String consume();
    public void send(String message);
}
