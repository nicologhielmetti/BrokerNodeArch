package com.sweng;



import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ServiceMetadata {

    private String methodName;
    private String owner;
    private String applicationField;
    private ArrayList<String> keywords=new ArrayList<>();
    private String description;
    private String activationDate;

    public ServiceMetadata(String methodName, String owner) {
        this.keywords = new ArrayList<>();
        this.methodName = methodName;
        this.owner = owner;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        activationDate = dateFormat.format(date);
    }

    static public ServiceMetadata fromJson(JsonObject json) {
        return (new Gson()).fromJson(json.toString(),ServiceMetadata.class);
        /*
        this.methodName=  json.get("methodName").getAsString();
        this.owner =  json.get("owner").getAsString();
        this.activationDate =  json.get("activationDate").getAsString();
        this.description =  json.getAsJsonPrimitive("description").isString()?json.getAsJsonPrimitive("description").getAsString():null;
        this.applicationField = json.get("applicationField").getAsString();
        JsonArray jsonKeywords = json.getAsJsonArray("keywords");
        for(JsonElement j:jsonKeywords){
            this.keywords.add(json.getAsString());
        }*/
    }

    public JsonObject toJson() {
        Gson gson=new Gson();
        return gson.fromJson(gson.toJson(this),JsonObject.class);
        /*
        JsonObject json = new JsonObject();
        json.addProperty("methodName", this.methodName);
        json.addProperty("owner", this.owner);
        json.addProperty("activationDate", this.activationDate);
        json.addProperty("description", this.description);
        json.addProperty("applicationField", this.applicationField);
        JsonArray jsonKeywords = new JsonArray();
        for (String k:keywords) {
            jsonKeywords.add(k);
        }
        json.add("keywords", jsonKeywords);
        return json;*/
    }

    public void setApplicationField(String applicationField) { this.applicationField = applicationField; }

    public void setMethodName(String methodName) { this.methodName = methodName; }

    public void setDescription(String description) { this.description = description; }

    public void setKeywords(ArrayList<String> keywords) { this.keywords = keywords; }

    public ArrayList<String> getKeywords() { return keywords; }

    public String getActivationDate() { return activationDate; }

    public String getApplicationField() { return applicationField; }

    public String getDescription() { return description; }

    public String getOwner() { return owner; }

    public String getMethodName() { return methodName; }

    public void addKeyword(String keyword) { this.keywords.add(keyword); }

    static void test(){
        Gson gson=new Gson();
        ServiceMetadata sm=new ServiceMetadata("sum","me");
        String s=gson.toJson(sm);
        System.out.println(s);
        sm=gson.fromJson(s,ServiceMetadata.class);
    }
}
