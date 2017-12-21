package searchstrategy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import service.ServiceMetadata;


public class TitleSearchStrategy extends SearchStrategy {
    private String title;

    public TitleSearchStrategy(String title){
        this.title = title;
    }

    @Override
    boolean filter(ServiceMetadata service) { return service.getMethodName().contains(title); }

    @Override
    public JsonElement toJsonElement(){
        return (new Gson()).fromJson("{\"type\":\"TitleSearchStrategy\",\"method\":"+title+"}",JsonElement.class);
    }
}
