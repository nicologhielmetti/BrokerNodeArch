package com.sweng;

import java.util.function.Function;

public class Service {

    private ServiceMetadata serviceMetadata;

    public Service(Function f) {
        this.serviceMetadata = new ServiceMetadata();
    }
}
