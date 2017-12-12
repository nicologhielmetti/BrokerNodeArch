package com.sweng;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class DummyBroker {

    public static void main(String[] args) {

        // Create new node obj
        IConnectionManager connectionManager = new ZeroMQConnectionManager(6789);
        Broker broker=new Broker(connectionManager);

        broker.start();
    }
}
