package com.sweng;

import org.json.simple.JSONObject;

public interface IServiceMethod {

    JSONObject run(JSONObject parameters) throws RuntimeException;

}
