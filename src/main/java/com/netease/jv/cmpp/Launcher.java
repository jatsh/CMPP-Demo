package com.netease.jv.cmpp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;


public class Launcher {

    private static final ThreadFactory NAMED_THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("cmpp-pool-%d").build();
    private static final ExecutorService SINGLE_THREAD_POOL = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024), NAMED_THREAD_FACTORY, new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) {
        String host = "";
        int port = 7071;
        String spCode = "";
        String sharedSecret = "";
        NettyClient client = new NettyClient(host, port, spCode, sharedSecret);
        SINGLE_THREAD_POOL.submit(client);
    }
}
