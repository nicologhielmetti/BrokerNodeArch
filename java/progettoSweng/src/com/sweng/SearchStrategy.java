package com.sweng;
import org.json.simple.JSONObject;


public abstract class SearchStrategy {

    public java.util.List<ServiceMetadata> filterList(java.util.List<ServiceMetadata> services){
        java.util.List<ServiceMetadata> result=new java.util.ArrayList<>();
        for (ServiceMetadata s:services)
            if (filter(s)) result.add(s);
        return result;
    }
    abstract boolean filter(ServiceMetadata service);
    abstract JSONObject toJson();

    static public SearchStrategy create(JSONObject j){
        switch (j.get("type").toString()){
            case "TitleSearchStrategy":
                return new TitleSearchStrategy(j.get("title").toString());
            case "OwnerSearchStrategy":
                return new OwnerSearchStrategy(j.get("owner").toString());
            case "KeywordSearchStrategy":
                return new KeywordSearchStrategy(j.get("keyword").toString());
        }
        //search strategy not implemented
        return null;
    }

    static public void test(){
        boolean ok=true;
        SearchStrategy s;

        s=SearchStrategy.create((new TitleSearchStrategy("sum")).toJson());
        if(!(s instanceof TitleSearchStrategy))ok=false;
        s=SearchStrategy.create((new OwnerSearchStrategy("me")).toJson());
        if(!(s instanceof OwnerSearchStrategy))ok=false;
        s=SearchStrategy.create((new KeywordSearchStrategy("math")).toJson());
        if(!(s instanceof KeywordSearchStrategy))ok=false;

        if(ok)System.out.println("SearchStrategy...ok!");
        else System.out.println("ERROR SearchStrategy create() method is broken");
    }
}
