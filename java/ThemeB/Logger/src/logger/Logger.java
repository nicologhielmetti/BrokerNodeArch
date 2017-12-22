package logger;

public class Logger {
    private static final boolean DEBUG = false;

    public static void log(String msg) {
        if (DEBUG) System.out.println("LOG: " + msg);
    }

    public static void error(String msg) {
        System.err.println("ERROR: " + msg);
    }

}
