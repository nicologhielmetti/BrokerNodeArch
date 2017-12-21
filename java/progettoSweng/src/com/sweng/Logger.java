package com.sweng;

public class Logger {
    private static final boolean DEBUG = true;

    public static void log(String msg){
        if(DEBUG)System.out.println("LOG: "+msg);
    }
}
