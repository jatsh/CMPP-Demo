package com.netease.jv.cmpp;

import com.netease.jv.cmpp.handler.*;
import io.netty.channel.socket.SocketChannel;

public class ChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {


    private final String spCode;

    private final String sharedSecret;

    public ChannelInitializer(String spCode, String sharedSecret) {
        this.spCode = spCode;
        this.sharedSecret = sharedSecret;
    }


    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast("decoder", new CMPPDecoder())
                .addLast("encoder", new CMPPEncoder())
                .addLast("heartbeat", new HeartBeatHandler())
                .addLast("loginHandler", new CMPPLoginHandler(spCode, sharedSecret))
                .addLast("messageHandler", new CMPPSubmitHandler());
    }
}
