package com.netease.jv.cmpp.handler;

import com.google.common.primitives.Bytes;
import com.netease.jv.cmpp.bean.CMPPDeliverMessage;
import com.netease.jv.cmpp.bean.CMPPDeliverRespMessage;
import com.netease.jv.cmpp.bean.CMPPMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static com.netease.jv.cmpp.bean.CMPPDeliverMessage.msgId2Bytes;

public class CMPPSubmitHandler extends SimpleChannelInboundHandler<CMPPMessage> {

    private static final Logger loggerException = LoggerFactory.getLogger(CMPPSubmitHandler.class);
    /**
     *
     */
    private final byte[] msgId = new byte[8];
    /**
     *
     */
    private final byte[] pkTotal = new byte[]{1};
    /**
     *
     */
    private final byte[] pkNumber = new byte[]{1};
    /**
     *
     */
    private final byte[] registeredDelivery = new byte[]{1};
    /**
     *
     */
    private final byte[] msgLevel = new byte[]{9};
    /**
     * 。
     */
    private final byte[] serviceId = Bytes.ensureCapacity(new byte[]{}, 10, 0);
    /**
     * 计费用户类型字段 0：对目的终端MSISDN计费； 1：对源终端MSISDN计费； 2：对SP计费; 3：表示本字段无效，对谁计费参见Fee_terminal_Id字段。
     */
    private final byte[] feeUserType = new byte[]{2};
    /**
     * 被计费用户的号码（如本字节填空，则表示本字段无效，对谁计费参见Fee_UserType字段，本字段与Fee_UserType字段互斥）
     */
    private final byte[] feeTerminalId = Bytes.ensureCapacity(new byte[]{}, 21, 0);

    /**
     * 0是普通GSM 类型，点到点方式 ,127 :写sim卡
     */
    private final byte[] tppId = new byte[]{0};
    /**
     * 0:msgcontent不带协议头。1:带有协议头
     */
    private final byte[] tpuDhi = new byte[]{0};
    /**
     * 信息格式
     */
    private final byte[] msgFmt = new byte[]{8};
    /**
     * 信息内容来源(SP_Id)
     */
    private final byte[] msgSrc = "903095".getBytes();
    /**
     * 资费类别 01：对“计费用户号码”免费 02：对“计费用户号码”按条计信息费 03：对“计费用户号码”按包月收取信息费 04：对“计费用户号码”的信息费封顶
     * 05：对“计费用户号码”的收费是由SP实现
     */
    private final byte[] feeType = "01".getBytes();
    /**
     * 资费代码（以分为单位
     */
    private final byte[] feeCode = "000000".getBytes();
    /**
     * 存活有效期，格式遵循SMPP3.3协议
     */
    private final byte[] valIdTime = Bytes.ensureCapacity(new byte[]{}, 17, 0);
    /**
     * 定时发送时间，格式遵循SMPP3.3协议
     */
    private final byte[] atTime = Bytes.ensureCapacity(new byte[]{}, 17, 0);
    /**
     * 源号码 SP的服务代码或前缀为服务代码的长号码, 网关将该号码完整的填到SMPP协议Submit_SM消息相应的source_addr字段，该号码最终在用户手机上显示为短消息的主叫号码
     */
    private final byte[] srcId = Bytes.ensureCapacity("1069021097".getBytes(), 21, 0);

    /**
     * 接收信息的用户数量(小于100个用户)
     */
    private final byte[] destUsrTl = new byte[]{1};
    /**
     * 接收短信的MSISDN号码
     */
    private final byte[] desTerminalId = Bytes.ensureCapacity("手机号xxxxxxxx".getBytes(), 21, 0);

    private final String msgContent = "【SIGN】短信文案";

    private final byte[] msgLength = new byte[1];
    private final byte[] reserve = new byte[8];


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CMPPMessage msg) {
        if (msg.getCommand() == CMPPMessage.Command.CMPP_CONNECT_RESP) {
            msgLength[0] = (byte) msgContent.getBytes(StandardCharsets.UTF_16BE).length;
            //登录成功
            byte[] content = Bytes.concat(
                    msgId,
                    pkTotal,
                    pkNumber,
                    registeredDelivery,
                    msgLevel,

                    serviceId,
                    feeUserType,
                    feeTerminalId,
                    tppId,
                    tpuDhi,
                    msgFmt,

                    msgSrc,
                    feeType,
                    feeCode,
                    valIdTime,
                    atTime,

                    srcId,
                    destUsrTl,
                    desTerminalId,
                    msgLength,

                    msgContent.getBytes(StandardCharsets.UTF_16BE),
                    reserve
            );
            CMPPMessage submitMessage = new CMPPMessage(CMPPMessage.Command.CMPP_SUBMIT, content);
            ctx.writeAndFlush(submitMessage);
        }

        if (msg.getCommand() == CMPPMessage.Command.CMPP_DELIVER) {
            CMPPDeliverMessage deliverMsg = new CMPPDeliverMessage(msg.getContent());
            loggerException.info("deliverMsg : {} ", deliverMsg);
            CMPPDeliverRespMessage response = new CMPPDeliverRespMessage(deliverMsg.getSerial(),
                    Bytes.concat(msgId2Bytes(deliverMsg.getMsgId()), new byte[4]));
            ctx.writeAndFlush(response);
        }
    }

}
