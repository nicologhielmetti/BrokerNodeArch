package JsonRpc;

import java.lang.String;

public interface IConncetion {
    public String receive();
    public void send(String msg);
}
