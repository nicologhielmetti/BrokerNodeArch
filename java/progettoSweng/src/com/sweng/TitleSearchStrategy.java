package com.sweng;

import org.json.simple.JSONObject;

public class TitleSearchStrategy extends SearchStrategy {
    private String title;

    public TitleSearchStrategy(String title){
        this.title=title;
    }

    @Override
    boolean filter(ServiceMetadata service) {
        return service.getMethodName()==title;
    }

    @Override
    public String toJson(){
        return "{\"type\"=\"TitleSearchStrategy\",\"title=\""+title+"\"}";
    }
}
