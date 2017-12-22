package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *  ServiceMetadata class contains all information about a service used by Node and Broker to identify services
 */
public class ServiceMetadata {

    private String method;
    private String owner;
    private String applicationField;
    private ArrayList<String> keywords;
    private String description;
    private String activationDate;


    /** ServiceMetadata constructor
     * @param method
     * @param owner
     * */
    public ServiceMetadata(String method, String owner) {
        this.keywords = new ArrayList<>();
        this.method = method;
        this.owner = owner;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        activationDate = dateFormat.format(date);
    }

    /**
     * This method is used to create a ServiceMetadata object from a JsonObject through GSON object deserialization
     * @param json
     * @return
     */
    static public ServiceMetadata fromJson(JsonObject json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ServiceMetadata.class);
    }

    /**
     * This method is used to create a JsonObject from a ServiceMetadata object through GSON object serialization
     * @return
     */
    public JsonObject toJson() {
        Gson gson=new Gson();
        return gson.fromJson(gson.toJson(this),JsonObject.class);
    }

    // Setter

    /** @param applicationField */
    public void setApplicationField(String applicationField) { this.applicationField = applicationField; }

    /** @param method */
    public void setMethodName(String method) { this.method = method; }

    /** @param description */
    public void setDescription(String description) { this.description = description; }

    /** @param keywords */
    public void setKeywords(ArrayList<String> keywords) {
        for (String k:keywords) {
            k = k.trim();
        }
        this.keywords = keywords;
    }

    /** @param keyword */
    public void addKeyword(String keyword) { this.keywords.add(keyword); }

    //Getter

    /** Keywords getter
     * @return
     */
    public ArrayList<String> getKeywords() { return keywords; }

    /** ActivationDate getter
     * @return
     */
    public String getActivationDate() { return activationDate; }

    /** ApplicationField getter
     * @return
     */
    public String getApplicationField() { return applicationField; }

    /** Description getter
     * @return
     */
    public String getDescription() { return description; }

    /** Owner getter
     * @return
     */
    public String getOwner() { return owner; }

    /** Method getter
     * @return
     */
    public String getMethodName() { return method; }
}
