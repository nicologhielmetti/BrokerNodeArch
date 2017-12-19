package com.sweng;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KeywordSearchStrategy extends SearchStrategy {
    private List<String> keywords=new ArrayList<>();

    public KeywordSearchStrategy(String keyword){
        this.keywords.add(keyword);
    }
    public KeywordSearchStrategy(List<String> keywords){
        this.keywords.addAll(keywords);
    }

    @Override
    boolean filter(ServiceMetadata service) {
        for(String k:keywords) {
            if (service.getKeywords().contains(k)) return true;
        }
        return false;
    }

    @Override
    public String toJson(){
        String s="{\"type\"=\"KeywordSearchStrategy\",\"keywords=[ ";
        for(String k:keywords)s+="\""+k+"\",";
        s=s.substring(0,s.length()-1)+"]}";
        return s;
    }
}
