package com.sweng;

public class Node {

    public Node() {}

    public void provideService(Service service) {
        ServiceMetadata sm = service.getServiceMetadata();
    }

}
