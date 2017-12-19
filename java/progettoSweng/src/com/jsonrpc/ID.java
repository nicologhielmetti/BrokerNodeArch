package com.jsonrpc;

public class ID {

    private Object value;

    public ID() { this.value = null; }
    public ID(String value) { this.value = value; }
    public ID (Integer value) { this.value = value; }
    static public ID Null(){return new ID();}

    public void set(String value) { this.value = value; }
    public void set(Integer value) { this.value = value; }
    public void setNull() { this.value = null; }

    public Object getValue() { return this.value; }
    public Integer getAsInt(){return (Integer)value; }
    public String getAsString(){return (String)value; }

    public boolean isInt() { return (this.value instanceof Integer); }
    public boolean isString() { return (this.value instanceof String); }
    public boolean isNull() { return (this.value == null); }

    @Override
    public String toString(){
        if(isInt())return String.valueOf(value);
        if(isString())return (String) value;
        if(isNull())return "null";
        return "";
    }

    @Override
    public boolean equals(Object id) {
        if (id == null) return this.isNull();
        if (id == this) return true;
        if(id instanceof String)return id==value;
        if(id instanceof Integer)return id==value;
        
        if (!(id instanceof ID)) return false;
        ID temp = (ID) id;
        if ((temp.isNull()) && (this.isNull())) return true;
        return temp.value==this.value;
    }

    static public void test(){
        ID a=new ID(1);
        ID b=new ID(1);
        assert(a.equals(b));
        assert(a.equals(1));
        assert(!a.equals("1"));
        assert(!a.equals(null));

        a.set(2);
        assert(!a.equals(b));
        assert(!a.equals("1"));
        assert(!a.equals(1));
        assert(!a.equals(null));

        a.set("1");
        assert(!a.equals(b));
        assert(a.equals("1"));
        assert(!a.equals(1));
        assert(!a.equals(null));

        b.set("1");
        assert(a.equals(b));

        a.set("2");
        assert(!a.equals(b));
        assert(!a.equals("1"));
        assert(!a.equals(1));
        assert(!a.equals(null));

        a.setNull();
        assert(!a.equals(b));
        assert(!a.equals("1"));
        assert(!a.equals(1));
        assert(a.equals(null));

        b.setNull();
        assert(a.equals(b));
        assert(!a.equals("1"));
        assert(!a.equals(1));
        assert(a.equals(null));
    }



}
