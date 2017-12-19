package com.sweng;

import com.jsonrpc.IConnection;
import org.json.simple.parser.ParseException;

public interface IConnectionManager {

    IConnection acceptConnection();
}
