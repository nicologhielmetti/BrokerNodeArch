package com.sweng;

import org.json.simple.JSONObject;

public class DummyServer {

    public static void main(String[] args) {

        // Create node obj
        IConnectionFactory connectionFactory = new IConnectionFactory();
        Node node = new Node(connectionFactory);

        // Provide a service
        ServiceMetadata sm = new ServiceMetadata("title", "owner");
        // Set all metadata through setter methods
        IServiceMethod s = new IServiceMethod() {
            @Override
            public JSONObject run(JSONObject parameters) {
                System.out.println("Service is running...");
                System.out.println("Service finish");
                return null;
            }
        };
        Service service = new Service(s, sm);
        node.provideService(service);

        // Delete an own service
        node.deleteService(service.getServiceMetadata().getTitle());
    }

}
