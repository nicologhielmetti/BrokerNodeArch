package jsonrpclibrary;

import connectioninterfaces.IConnection;
import connectioninterfaces.TimeoutException;

public class JsonRpcManager {
    private IConnection connection;

    public JsonRpcManager(IConnection connection) {
        this.connection = connection;
    }

    public JsonRpcMessage listenRequest() throws ParseException {
        JsonRpcMessage msg = null;
        do {
            try {
                msg = listen(-1);
            } catch (TimeoutException e) {
                e.printStackTrace(); //should never happen
            }
        } while (!(msg instanceof JsonRpcRequest) && !(msg instanceof JsonRpcBatchRequest));
        connection.consume();
        return msg;
    }

    public JsonRpcMessage listenRequest(long milliseconds) throws ParseException, TimeoutException {
        JsonRpcMessage msg;
        long tStart = System.currentTimeMillis();
        long tDelta = 0;
        do {
            msg = listen(milliseconds - tDelta);
            tDelta=System.currentTimeMillis()-tStart;
        } while (!(msg instanceof JsonRpcRequest) && !(msg instanceof JsonRpcBatchRequest) && tDelta<milliseconds);

        if(msg instanceof JsonRpcRequest || msg instanceof JsonRpcBatchRequest){
            connection.consume();
            return msg;
        }
        else throw new TimeoutException("");

    }

    public JsonRpcMessage listenResponse() throws ParseException {
        JsonRpcMessage msg = null;
        do {
            try {
                msg = listen(-1);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        } while (!(msg instanceof JsonRpcResponse) && !(msg instanceof JsonRpcBatchResponse));
        connection.consume();
        return msg;
    }

    public JsonRpcMessage listenResponse(long milliseconds) throws ParseException, TimeoutException {
        JsonRpcMessage msg;
        long tStart = System.currentTimeMillis();
        long tDelta = 0;
        do {
            msg = listen(milliseconds - tDelta);
            tDelta=System.currentTimeMillis()-tStart;
        } while(!(msg instanceof JsonRpcResponse) && !(msg instanceof JsonRpcBatchResponse) && tDelta<milliseconds);

        if(msg instanceof JsonRpcResponse || msg instanceof JsonRpcBatchResponse){
            connection.consume();
            return msg;
        }
        else throw new TimeoutException("");
    }

    /**
     * @param milliseconds: If nothing arrive in that time a TimeoutException is thrown.
     *                      If it is a negative number it waits forever.
     * @return a JsonRpcMessage object if a valid message arrive within the timeout
     * @throws ParseException   if a not well-formed json-rpc message is received
     * @throws TimeoutException if nothing arrive within the timeout
     */
    private JsonRpcMessage listen(long milliseconds) throws ParseException, TimeoutException {
        String input = milliseconds >= 0 ? connection.read(milliseconds) : connection.read();
        if (input == null) throw new TimeoutException("");

        input = input.trim();
        JsonRpcMessage msg;

        if (input.charAt(0) == '[') {
            msg = JsonRpcBatchRequest.fromJson(input);
            if (msg != null) return msg;
            msg = JsonRpcBatchResponse.fromJson(input);
            if (msg != null) return msg;
            //error
            connection.consume();
            throw new ParseException("\"" + input + "\" is not a valid json-rpc batch");
        } else {
            msg = JsonRpcRequest.fromJson(input);
            if (msg != null) return msg;
            msg = JsonRpcResponse.fromJson(input);
            if (msg != null) return msg;
            connection.consume();
            throw new ParseException("\"" + input + "\" is not a valid json-rpc message");
        }
    }

    public IConnection getConnection(){
        return connection;
    }
    public void send(JsonRpcMessage msg) {
        connection.send(msg.toString());
    }
}
