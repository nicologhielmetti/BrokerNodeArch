package jsonrpclibrary;

public abstract class JsonRpcMessage {
    public boolean isBatch(){
        return this instanceof JsonRpcBatchRequest || this instanceof JsonRpcBatchResponse;
    }
}
