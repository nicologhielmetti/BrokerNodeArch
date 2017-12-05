package com.jsonrpc;

import org.json.simple.JSONObject;

public abstract class JsonRpcMessage {
    protected JSONObject json;

    public String toJsonRpcString(){
        return json.toJSONString();
    }

    public JSONObject toJsonRpcObject(){
        return json;
    }
}
