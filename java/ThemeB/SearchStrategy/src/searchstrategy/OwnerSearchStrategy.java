package searchstrategy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import service.ServiceMetadata;

public class OwnerSearchStrategy extends SearchStrategy {
    private String owner;

    public OwnerSearchStrategy(String owner) {
        this.owner = owner;
    }

    @Override
    boolean filter(ServiceMetadata service) {
        return service.getOwner().contains(owner);
    }

    @Override
    public JsonElement toJsonElement() {
        return (new Gson()).fromJson("{\"type\":\"OwnerSearchStrategy\",\"owner\":" + owner + "}", JsonElement.class);
    }
}
