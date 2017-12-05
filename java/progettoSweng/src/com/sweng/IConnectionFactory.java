package com.sweng;
import com.jsonrpc.IConnection;

public interface IConnectionFactory {

    IConnection createConnection();

}
