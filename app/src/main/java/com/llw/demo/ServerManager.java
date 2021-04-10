package com.llw.demo;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.util.concurrent.TimeUnit;

public class ServerManager {

    private Server mServer;

    /**
     * Create server.
     */
    public ServerManager() {
        mServer = AndServer.serverBuilder()
                .port(8080)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onStopped() {
                    }

                    @Override
                    public void onException(Exception e) {
                    }
                })
                .build();
    }

    /**
     * Start server.
     */
    public void startServer() {
        mServer.startup();
    }

    /**
     * Stop server.
     */
    public void stopServer() {
        mServer.shutdown();
    }
}