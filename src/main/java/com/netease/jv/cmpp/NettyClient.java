package com.netease.jv.cmpp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * The type Netty client.
 */
public class NettyClient implements Runnable {

    private static final Logger loggerException = LoggerFactory.getLogger(NettyClient.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final String host;
    private final int port;
    private final String spCode;
    private final String sharedSecret;

    /**
     * Instantiates a new Netty client.
     *
     * @param host         the host
     * @param port         the port
     * @param spCode       the sp code
     * @param sharedSecret the shared secret
     */
    public NettyClient(String host, int port, String spCode, String sharedSecret) {
        this.host = host;
        this.port = port;
        this.spCode = spCode;
        this.sharedSecret = sharedSecret;
    }

    /**
     * Gets serial.
     *
     * @return the serial
     */
    public static int getSerial() {
        return RANDOM.nextInt();
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootStrap = new Bootstrap();
            bootStrap
                    .group(group)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer(spCode, sharedSecret));

            ChannelFuture channelFuture = bootStrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            loggerException.error("", e);
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                loggerException.error("", e);
            }
        }
    }
}
