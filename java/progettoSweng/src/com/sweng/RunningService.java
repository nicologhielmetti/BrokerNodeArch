package com.sweng;

import com.jsonrpc.JsonRpcManager;

public class RunningService {

    private Service service;
    private JsonRpcManager manager;
    private Thread thread;

    public RunningService(Service service, JsonRpcManager manager, Thread thread) {
        this.service = service;
        this.manager = manager;
        this.thread = thread;
    }

    public Service getService() { return this.service; }

    public JsonRpcManager getJsonRpcManager() { return this.manager; }

    public Thread getThread() { return this.thread; }

    public void delete() {
        this.service = null;
        this.manager = null;
        this.thread.interrupt();
        this.thread = null;
    }

}
