package com.netease.jv.cmpp.bean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Setter
@Getter
public class CMPPDeliverMessage extends CMPPMessage {

    public static final int DEFAULT_FRAME_LENGTH = 32;
    private static final Logger loggerException = LoggerFactory.getLogger(CMPPDeliverMessage.class);
    private static final Charset DEFAULT_TRANSPORT_CHARSET = StandardCharsets.UTF_8;
    private final MsgId msgId;

    private final String destId;

    private final String serviceId;

    private final short tpPid;

    private final short tpUdhi;

    private final short msgFmt;

    private final String srcTerminalId;

    private final String linkId;
    private short registeredDelivery;
    private short msgLength;
    private byte[] content;
    private String contentString;
    private CmppReportMessage reportMessage;

    public CMPPDeliverMessage(byte[] content) {
        super(Command.CMPP_DELIVER, content);

        ByteBuf bodyBuffer = Unpooled.wrappedBuffer(content);
        try {
            this.msgId = bytes2MsgId(toArray(bodyBuffer, MsgId.LENGTH));
            this.destId = bodyBuffer.readCharSequence(21, DEFAULT_TRANSPORT_CHARSET).toString().trim();
            this.serviceId = bodyBuffer.readCharSequence(10, DEFAULT_TRANSPORT_CHARSET).toString().trim();
            this.tpPid = bodyBuffer.readUnsignedByte();
            this.tpUdhi = bodyBuffer.readUnsignedByte();
            this.msgFmt = bodyBuffer.readUnsignedByte();
            this.srcTerminalId = bodyBuffer.readCharSequence(21, DEFAULT_TRANSPORT_CHARSET).toString()
                    .trim();
            this.registeredDelivery = bodyBuffer.readUnsignedByte();
            int frameLength = (short) (bodyBuffer.readUnsignedByte() & 0xffff);

            if (registeredDelivery == 0) {
                byte[] contentBytes = new byte[frameLength];
                bodyBuffer.readBytes(contentBytes);
                this.contentString = new String(contentBytes);
                this.msgLength = (short) frameLength;
            } else {
                if (frameLength != DEFAULT_FRAME_LENGTH) {
                    this.setReportMessage(new CmppReportMessage());
                    this.getReportMessage().setMsgId(bytes2MsgId(toArray(bodyBuffer, MsgId.LENGTH)));
                    this.getReportMessage().setStat(
                            bodyBuffer.readCharSequence(7,
                                    DEFAULT_TRANSPORT_CHARSET).toString().trim());
                    this.getReportMessage().setSubmitTime(
                            bodyBuffer.readCharSequence(10,
                                    DEFAULT_TRANSPORT_CHARSET).toString().trim());
                    this.getReportMessage().setDoneTime(
                            bodyBuffer.readCharSequence(10,
                                    DEFAULT_TRANSPORT_CHARSET).toString().trim());
                    this.getReportMessage().setDestTerminalId(
                            bodyBuffer.readCharSequence(21,
                                    DEFAULT_TRANSPORT_CHARSET).toString().trim());
                    this.getReportMessage().setSmscSequence(bodyBuffer.readUnsignedInt());
                }
            }
            //卓望发送的状态报告 少了11个字节， 剩下的字节全部读取
            this.linkId = bodyBuffer
                    .readCharSequence(bodyBuffer.readableBytes(), DEFAULT_TRANSPORT_CHARSET)
                    .toString().trim();
        } finally {
            bodyBuffer.release();
        }
    }

    public static MsgId bytes2MsgId(byte[] bytes) {
        assert (bytes.length == MsgId.LENGTH);
        long result = ByteBuffer.wrap(bytes).getLong();
        MsgId msgId = new MsgId();
        msgId.setMonth((int) ((result >>> 60) & 0xf));
        msgId.setDay((int) ((result >>> 55) & 0x1f));
        msgId.setHour((int) ((result >>> 50) & 0x1f));
        msgId.setMinutes((int) ((result >>> 44) & 0x3f));
        msgId.setSeconds((int) ((result >>> 38) & 0x3f));
        msgId.setGateId((int) ((result >>> 16) & 0x3fffff));
        msgId.setSequenceId((int) (result & 0xffff));
        return msgId;
    }

    public static byte[] msgId2Bytes(MsgId msgId) {
        byte[] bytes = new byte[MsgId.LENGTH];
        long result = 0;
        if (msgId != null) {
            result |= (long) msgId.getMonth() << 60L;
            result |= (long) msgId.getDay() << 55L;
            result |= (long) msgId.getHour() << 50L;
            result |= (long) msgId.getMinutes() << 44L;
            result |= (long) msgId.getSeconds() << 38L;
            result |= (long) msgId.getGateId() << 16L;
            result |= (long) msgId.getSequenceId() & 0xffffL;
        }
        ByteBuffer.wrap(bytes).putLong(result);
        return bytes;

    }

    public static byte[] toArray(ByteBuf buf, int length) {
        byte[] result = new byte[length];
        buf.readBytes(result);
        return result;
    }

    @Override
    public String toString() {
        return "CMPPDeliverMessage{" +
                "msgId=" + msgId +
                ", destId='" + destId + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", tpPid=" + tpPid +
                ", tpUdhi=" + tpUdhi +
                ", msgFmt=" + msgFmt +
                ", srcTerminalId='" + srcTerminalId + '\'' +
                ", linkId='" + linkId + '\'' +
                ", registeredDelivery=" + registeredDelivery +
                ", msgLength=" + msgLength +
                ", reportMessage=" + reportMessage +
                "} ";
    }

    @Data
    public static class MsgId {

        public static final int LENGTH = 8;
        private int month;
        private int day;
        private int hour;
        private int minutes;
        private int seconds;
        private int gateId;
        private int sequenceId;
    }

    @Data
    public static class CmppReportMessage {

        private MsgId msgId = new MsgId();
        private String stat = "";
        private String submitTime = String
                .format("%ty%<tm%<td%<tH%<tM", new Date());
        private String doneTime = String
                .format("%ty%<tm%<td%<tH%<tM", new Date());
        private String destTerminalId = "";
        private long smscSequence = 0;
    }
}
