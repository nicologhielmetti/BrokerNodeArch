package com.sweng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

public class ServiceMetadata {

    private String methodName;
    private String owner;
    private String applicationField;
    private ArrayList<String> keywords;
    private String description;
    private String activationDate;
    private String signature; // change to Json string

    public ServiceMetadata(String methodName, String owner) {
        this.keywords.clear();
        this.methodName = methodName;
        this.owner = owner;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        activationDate = dateFormat.format(date);
    }

    public ServiceMetadata(JSONObject json) {
        this.methodName= (String) json.get("methodName");
        this.owner = (String) json.get("owner");
        this.activationDate = (String) json.get("activationDate");
        this.description = (String) json.get("description");
        this.applicationField = (String) json.get("applicationField");
        this.signature = (String) json.get("signature");
        JSONArray jsonKeywords = (JSONArray) json.get("keywords");
        Iterator<String> iterator = jsonKeywords.iterator();
        while (iterator.hasNext()) {
            this.keywords.add(iterator.next());
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("methodName", this.methodName);
        json.put("owner", this.owner);
        json.put("activationDate", this.activationDate);
        json.put("description", this.description);
        json.put("applicationField", this.applicationField);
        json.put("signature", this.signature);
        JSONArray jsonKeywords = new JSONArray();
        jsonKeywords.addAll(keywords);
        json.put("keywords", jsonKeywords);
        return json;
    }

    public void setApplicationField(String applicationField) { this.applicationField = applicationField; }

    public void setMethodName(String methodName) { this.methodName = methodName; }

    public void setDescription(String description) { this.description = description; }

    public void setKeywords(ArrayList<String> keywords) { this.keywords = keywords; }

    public void setSignature(String signature) { this.signature = signature; }

    public ArrayList<String> getKeywords() { return keywords; }

    public String getActivationDate() { return activationDate; }

    public String getApplicationField() { return applicationField; }

    public String getDescription() { return description; }

    public String getOwner() { return owner; }

    public String getSignature() { return signature; }

    public String getMethodName() { return methodName; }

    public void addKeyword(String keyword) { this.keywords.add(keyword); }
}
