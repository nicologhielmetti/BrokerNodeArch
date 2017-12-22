package jsonrpclibrary;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class JsonRpcBatchRequest extends JsonRpcMessage {
    private List<JsonRpcRequest> batch = new ArrayList<>();

    public JsonRpcBatchRequest() {
    }

    public void add(JsonRpcRequest request) throws NullPointerException {
        if (request == null) throw new NullPointerException();
        batch.add(request);
    }

    public void add(List<JsonRpcRequest> requests) {
        for (JsonRpcRequest r : requests) {
            if (r == null) throw new NullPointerException();
        }
        batch.addAll(requests);
    }

    public String toString() {
        return toJson();
    }

    public String toJson() {
        String str = "[ ";
        for (JsonRpcRequest r : batch) {
            if (r != null) str += r.toJson() + ",";
        }
        return str.substring(0, str.length() - 1) + "]";
    }

    public static JsonRpcBatchRequest fromJson(String str) {
        JsonArray array = (new Gson()).fromJson(str, JsonArray.class);
        if (array == null) return null;
        JsonRpcBatchRequest batch = new JsonRpcBatchRequest();
        for (JsonElement e : array) {
            JsonRpcRequest r = JsonRpcRequest.fromJson(e.toString());
            if (r == null) r = JsonRpcRequest.invalid();
            batch.add(r);
        }
        if (batch.isEmpty()) return null;
        return batch;
    }

    public List<JsonRpcRequest> get() {
        return batch;
    }

    public boolean isEmpty() {
        return batch.isEmpty();
    }
}