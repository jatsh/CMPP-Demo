package com.netease.jv.cmpp.handler;


import com.google.common.primitives.Bytes;
import com.netease.jv.cmpp.bean.CMPPMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

public class CMPPLoginHandler extends SimpleChannelInboundHandler<CMPPMessage> {

    private static final Logger loggerException = LoggerFactory.getLogger(CMPPLoginHandler.class);

    private final String spCode;
    private final String sharedSecret;


    public CMPPLoginHandler(String spCode, String sharedSecret) {
        this.spCode = spCode;
        this.sharedSecret = sharedSecret;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        String timestamp = DateFormatUtils.format(new Date(), "MMddHHmmss");
        byte version = 0x20;
        byte[] userBytes = spCode.getBytes(StandardCharsets.UTF_8);
        byte[] passwdBytes = sharedSecret.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes = timestamp.getBytes(StandardCharsets.UTF_8);
        byte[] authenticatorSource = MessageDigest.getInstance("MD5").digest(Bytes.concat(userBytes, new byte[9], passwdBytes, timestampBytes));

        byte[] content = Bytes.concat(spCode.getBytes(), authenticatorSource, new byte[]{version},
                intToBytes(Integer.parseInt(timestamp)));


        CMPPMessage loginMessage = new CMPPMessage(CMPPMessage.Command.CMPP_CONNECT, content);
        ctx.writeAndFlush(loginMessage);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CMPPMessage msg) {
        //不处理任何信息，往后抛
        ctx.fireChannelRead(msg);
        if (msg.getCommand() == CMPPMessage.Command.CMPP_CONNECT_RESP) {
            ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getContent());
            try {
                short status = bodyBuffer.readByte();
                String authenticator = bodyBuffer.readCharSequence(16, StandardCharsets.UTF_8).toString();
                short version = bodyBuffer.readByte();
                loggerException.info("login response : status = {}, authenticatorISMG = {} , version = {}", status, authenticator, version);
            } finally {
                bodyBuffer.release();
            }
        }
    }

    /**
     * 将int数值转换为占四个字节的byte数组
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) (value & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[0] = (byte) ((value >> 24) & 0xFF);
        return src;
    }
}
