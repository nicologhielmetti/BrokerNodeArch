package com.sweng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;


public abstract class SearchStrategy {

    public java.util.List<ServiceMetadata> filterList(java.util.List<ServiceMetadata> services){
        java.util.List<ServiceMetadata> result=new java.util.ArrayList<>();
        for (ServiceMetadata s:services)
            if (filter(s)) result.add(s);
        return result;
    }
    abstract boolean filter(ServiceMetadata service);
    abstract String toJson();

    static public SearchStrategy fromJson(String str){
        JsonObject j=(new Gson()).fromJson(str,JsonObject.class);
        switch (j.get("type").getAsString()){
            case "TitleSearchStrategy":
                return new TitleSearchStrategy(j.get("title").getAsString());
            case "OwnerSearchStrategy":
                return new OwnerSearchStrategy(j.get("owner").getAsString());
            case "KeywordSearchStrategy":{
                List<String> keywords=new ArrayList<>();
                JsonArray ja=j.get("keywords").getAsJsonArray();
                for(JsonElement k:ja)keywords.add(k.getAsString());
                return new KeywordSearchStrategy(keywords);
            }

        }
        //search strategy not implemented
        return null;
    }

    static public void test(){
        boolean ok=true;
        SearchStrategy s;

        s=SearchStrategy.fromJson((new TitleSearchStrategy("sum")).toJson());
        if(!(s instanceof TitleSearchStrategy))ok=false;
        s=SearchStrategy.fromJson((new OwnerSearchStrategy("me")).toJson());
        if(!(s instanceof OwnerSearchStrategy))ok=false;
        s=SearchStrategy.fromJson((new KeywordSearchStrategy("math")).toJson());
        if(!(s instanceof KeywordSearchStrategy))ok=false;

        if(ok)System.out.println("SearchStrategy...ok!");
        else System.out.println("ERROR SearchStrategy create() method is broken");
    }
}
