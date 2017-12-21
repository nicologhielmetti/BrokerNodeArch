package connectioninterfaces;


import java.lang.String;

public interface IConnection {
    String read();
    String read(long milliseconds) throws TimeoutException;
    void consume();
    void send(String message);
    void close();
}
