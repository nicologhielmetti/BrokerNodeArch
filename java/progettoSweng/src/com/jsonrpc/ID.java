package com.jsonrpc;

import org.json.simple.JSONObject;

public class ID extends JSONObject {

    private Object id;

    public ID() { this.id = null; }

    public ID(String id) { this.id = id; }

    public ID (Integer id) { this.id = id; }

    public void set(String id) { this.id = id; }

    public void set(Integer id) { this.id = id; }

    public void setNull() { this.id = null; }

    public Object getId() { return this.id; }

    public boolean isInt() { return (this.id instanceof Integer); }

    public boolean isString() { return (this.id instanceof String); }

    public boolean isNull() { return (this.id == null); }

    @Override
    public boolean equals(Object id) {
        if (id == null) return false;
        if (id == this) return true;
        if (!(id instanceof ID)) return false;
        ID temp = (ID) id;
        if ((temp.isInt()) && (this.isInt()) && ((Integer) this.id == (Integer) temp.getId())) return true;
        if ((temp.isString()) && (this.isString()) && (this.id == temp.getId())) return true;
        if ((temp.isNull()) && (this.isNull())) return true;
        return false;
    }
}
