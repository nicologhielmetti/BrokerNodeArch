package com.sweng;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.json.simple.JSONObject;

public class TitleSearchStrategy extends SearchStrategy {
    private String title;

    public TitleSearchStrategy(String title){
        this.title = title;
    }

    @Override
    boolean filter(ServiceMetadata service) { return service.getMethodName().equals(title); }

    @Override
    public JsonElement toJsonElement(){
        return (new Gson()).fromJson("{\"type\":\"TitleSearchStrategy\",\"methodName\":"+title+"}",JsonElement.class);
    }
}
