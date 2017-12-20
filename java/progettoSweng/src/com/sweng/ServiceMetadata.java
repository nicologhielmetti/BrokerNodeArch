package com.sweng;



import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ServiceMetadata {

    private String method;
    private String owner;
    private String applicationField;
    private ArrayList<String> keywords;
    private String description;
    private String activationDate;

    public ServiceMetadata(String method, String owner) {
        this.keywords = new ArrayList<>();
        this.method = method;
        this.owner = owner;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        activationDate = dateFormat.format(date);
    }

    static public ServiceMetadata fromJson(JsonObject json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ServiceMetadata.class);
    }

    public JsonObject toJson() {
        Gson gson=new Gson();
        return gson.fromJson(gson.toJson(this),JsonObject.class);
    }

    public void setApplicationField(String applicationField) { this.applicationField = applicationField; }

    public void setMethodName(String method) { this.method = method; }

    public void setDescription(String description) { this.description = description; }

    public void setKeywords(ArrayList<String> keywords) {
        for (String k:keywords) {
            k = k.trim();
        }
        this.keywords = keywords;
    }

    public ArrayList<String> getKeywords() { return keywords; }

    public String getActivationDate() { return activationDate; }

    public String getApplicationField() { return applicationField; }

    public String getDescription() { return description; }

    public String getOwner() { return owner; }

    public String getMethodName() { return method; }


    public void addKeyword(String keyword) { this.keywords.add(keyword); }

    static void test(){
        Gson gson=new Gson();
        ServiceMetadata sm=new ServiceMetadata("sum","me");
        String s=gson.toJson(sm);
        System.out.println(s);
        sm=gson.fromJson(s,ServiceMetadata.class);
    }
}
