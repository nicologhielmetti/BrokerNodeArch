package com.jsonrpc;

import java.lang.String;

public interface IConncetion {
    public String receive();
    public void send(String msg);
}
