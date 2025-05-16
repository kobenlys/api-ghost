package com.apighost.scenario.util;

import java.net.http.WebSocket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketRetryScheduler {

    private final ScheduledExecutorService scheduleExecutor =
        Executors.newSingleThreadScheduledExecutor();
    private final Queue<Map.Entry<Long, WebSocket>> retryQueue;
    private volatile boolean started = false;

    public WebSocketRetryScheduler(Queue<Map.Entry<Long, WebSocket>> retryQueue) {
        this.retryQueue = retryQueue;
    }

    public void start(){
        if (started) {
            return;
        }

        synchronized (this) {
            if (!started) {
                started = true;
                scheduleExecutor.scheduleAtFixedRate(() -> {
                    if (!retryQueue.isEmpty()){
                        retryFailedCloses();
                    };
                }, 0, 5, TimeUnit.MINUTES);
            }
        }
    }

    private void retryFailedCloses(){
        int size = retryQueue.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<Long, WebSocket> ws = retryQueue.poll();
            if (ws == null) break;
            try {
                ws.getValue().sendClose(WebSocket.NORMAL_CLOSURE, "Clear");
            } catch (Exception e) {
                retryQueue.add(ws);
            }
        }
    }

    public void stop() {
        scheduleExecutor.shutdown();
    }
}
