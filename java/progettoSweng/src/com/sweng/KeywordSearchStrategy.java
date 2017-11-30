package com.sweng;

import org.json.simple.JSONObject;

public class KeywordSearchStrategy extends SearchStrategy {
    private String keyword;

    public KeywordSearchStrategy(String keyword){
        this.keyword=keyword;
    }

    @Override
    boolean filter(ServiceMetadata service) {
        return service.getKeywords().contains(keyword);
    }

    @Override
    public JSONObject toJson(){
        JSONObject o=new JSONObject();
        o.put("type","KeywordSearchStrategy");
        o.put("keyword",keyword);
        return o;
    }
}
