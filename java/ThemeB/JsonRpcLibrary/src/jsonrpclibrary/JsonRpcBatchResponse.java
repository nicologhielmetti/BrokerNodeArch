package jsonrpclibrary;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class JsonRpcBatchResponse extends JsonRpcMessage {
    List<JsonRpcResponse> batch = new ArrayList<>();

    public JsonRpcBatchResponse() {
    }

    public void add(JsonRpcResponse response) throws NullPointerException {
        if (response == null) throw new NullPointerException();
        batch.add(response);
    }

    public void add(List<JsonRpcResponse> responses) {
        for (JsonRpcResponse r : responses) {
            if (r == null) throw new NullPointerException();
        }
        batch.addAll(responses);
    }

    public String toString() {
        return toJson();
    }

    public String toJson() {
        String str = "[ ";
        for (JsonRpcResponse r : batch) {
            if (r != null) str += r.toJson() + ",";
        }
        return str.substring(0, str.length() - 1) + "]";
    }

    public static JsonRpcBatchResponse fromJson(String str) {
        JsonArray array = (new Gson()).fromJson(str, JsonArray.class);
        if (array == null) return null;
        JsonRpcBatchResponse batch = new JsonRpcBatchResponse();
        for (JsonElement e : array) {
            JsonRpcResponse r = JsonRpcResponse.fromJson(e.toString());
            if (r == null) return null;
            batch.add(r);
        }
        if (batch.isEmpty()) return null;
        return batch;

    }

    public List<JsonRpcResponse> get() {
        return batch;
    }

    public boolean isEmpty() {
        return batch.isEmpty();
    }
}
