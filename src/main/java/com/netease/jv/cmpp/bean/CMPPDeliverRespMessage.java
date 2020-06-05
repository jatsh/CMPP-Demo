package com.netease.jv.cmpp.bean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import com.netease.jv.cmpp.bean.CMPPDeliverMessage.MsgId;

import static com.netease.jv.cmpp.bean.CMPPDeliverMessage.bytes2MsgId;
import static com.netease.jv.cmpp.bean.CMPPDeliverMessage.toArray;


public class CMPPDeliverRespMessage extends CMPPMessage {

    private final MsgId msgId;

    private final int result;

    public CMPPDeliverRespMessage(int serial, byte[] content) {
        super(PROTOCOL_HEAD_LENGTH, Command.CMPP_DELIVER, serial, content);
        ByteBuf bodyBuffer = Unpooled.wrappedBuffer(content);
        try {
            this.msgId = bytes2MsgId(toArray(bodyBuffer, MsgId.LENGTH));
            this.result = bodyBuffer.readInt();
        } finally {
            bodyBuffer.release();
        }
    }

    @Override
    public String toString() {
        return "CMPPDeliverRespMessage{" +
                "msgId=" + msgId +
                ", result=" + result +
                "} ";
    }
}
