package com.sweng;

import org.json.simple.JSONObject;
import java.util.concurrent.TimeUnit;

public class DummyServer {

    public static void main(String[] args) {

        // Create node obj
        ZeroMQConnectionFactory connectionFactory = new ZeroMQConnectionFactory("tcp://localhost:6789");
        Node node = new Node(connectionFactory);

        // Provide a service
        ServiceMetadata sm = new ServiceMetadata("sum", "owner");
        // Set all metadata through setter methods
        IServiceMethod s = new IServiceMethod() {
            @Override
            public JSONObject run(JSONObject parameters) {
                System.out.println("Service is running...");
                JSONObject result=new JSONObject();
                int num1=Integer.parseInt(parameters.get("num1").toString());
                int num2=Integer.parseInt(parameters.get("num2").toString());

                result.put("result",Integer.toString(num1+num2));
                System.out.println("Service finish");
                return null;
            }
        };
        Service service = new Service(s, sm);
        System.out.println("Service created");

        node.provideService(service);


        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Delete an own service
        node.deleteService(service.getServiceMetadata().getMethodName());
    }

}
