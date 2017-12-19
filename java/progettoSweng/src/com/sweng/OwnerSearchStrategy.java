package com.sweng;

import org.json.simple.JSONObject;

public class OwnerSearchStrategy extends SearchStrategy {
    private String owner;

    public OwnerSearchStrategy(String owner){
        this.owner=owner;
    }

    @Override
    boolean filter(ServiceMetadata service) {
        return service.getOwner()==owner;
    }
    @Override
    public String toJson(){
        return "{\"type\"=\"OwnerSearchStrategy\",\"owner=\""+owner+"\"}";
    }
}
