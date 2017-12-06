package com.jsonrpc;

import java.lang.String;

public interface IConnection {
    public String read();
    public void consume();
    public void send(String message);

    public void close();
}
