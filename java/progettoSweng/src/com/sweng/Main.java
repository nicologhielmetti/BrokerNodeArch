package com.sweng;

import org.json.simple.JSONObject;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        ServiceMetadata sm = new ServiceMetadata("title", "owner");
        ServiceMethod s = new ServiceMethod() {
            @Override
            public JSONObject run(JSONObject parameters) {
                System.out.println("Service is running...");
                System.out.println("Service finish");
                return null;
            }
        };
        Service service = new Service(s, sm);
        service.processRequest(null);
    }
}
