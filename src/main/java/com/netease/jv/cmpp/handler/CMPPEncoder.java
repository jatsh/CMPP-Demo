package com.netease.jv.cmpp.handler;

import com.netease.jv.cmpp.bean.CMPPMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMPPEncoder extends MessageToByteEncoder<CMPPMessage> {

    private static final Logger loggerException = LoggerFactory.getLogger(CMPPEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, CMPPMessage msg, ByteBuf out) {
        out.ensureWritable(msg.getTotalLength());
        out.writeInt(msg.getTotalLength());
        out.writeInt(msg.getCommand().getValue());
        out.writeInt(msg.getSerial());
        if (msg.getContent().length > 0) {
            out.writeBytes(msg.getContent());
        }
        loggerException.info("send msg {} ", msg);
    }
}
