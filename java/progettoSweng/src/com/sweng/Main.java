package com.sweng;

import org.json.simple.JSONObject;

import com.jsonrpc.ID;

public class Main {

    public static void main(String[] args) {
        ID a = new ID();
        a.setNull();
        ID b = new ID();
        b.setNull();
        if (a.equals(b)) {
            System.out.println("true");
        }
        /*
        System.out.println("Hello world!");

        ServiceMetadata sm = new ServiceMetadata("title", "owner");
        IServiceMethod s = new IServiceMethod() {
            @Override
            public JSONObject run(JSONObject parameters) {
                System.out.println("Service is running...");
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException I) { }
                System.out.println("Service finish");
                return null;
            }
        };
        Service service = new Service(s, sm);
        service.processRequest(null);*/
    }
}
