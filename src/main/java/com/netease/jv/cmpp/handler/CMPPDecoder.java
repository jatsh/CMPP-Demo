package com.netease.jv.cmpp.handler;

import com.netease.jv.cmpp.bean.CMPPMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMPPDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger loggerException = LoggerFactory.getLogger(CMPPDecoder.class);

    public CMPPDecoder() {
        super(Integer.MAX_VALUE, 0, 4, -4, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);

        if (frame == null) {
            return null;
        }
        int headLength = frame.readInt();
        int command = frame.readInt();
        int serial = frame.readInt();

        byte[] data = new byte[headLength - CMPPMessage.PROTOCOL_HEAD_LENGTH];
        frame.readBytes(data);

        try {
            CMPPMessage.Command action = CMPPMessage.Command.valueOf(command);
            CMPPMessage msg = new CMPPMessage(headLength, action, serial, data);
            loggerException.info("receive :{}", msg);
            return msg;
        } finally {
            frame.release();
        }
    }

}
