package com.sweng;



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

    public ServiceMetadata(JsonObject json) {
        this.methodName=  json.get("methodName").getAsString();
        this.owner =  json.get("owner").getAsString();
        this.activationDate =  json.get("activationDate").getAsString();
        this.description =  json.get("description").getAsString();
        this.applicationField = json.get("applicationField").getAsString();
        JsonArray jsonKeywords = json.getAsJsonArray("keywords");
        for(JsonElement j:jsonKeywords){
            this.keywords.add(json.getAsString());
        }
    }

    public String toJson() {
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
        return json.toString();
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
}
