package com.jsonrpc;

import java.lang.String;

public interface IConnection {
    String read();
    void consume();
    void send(String message);
    void close();
}
