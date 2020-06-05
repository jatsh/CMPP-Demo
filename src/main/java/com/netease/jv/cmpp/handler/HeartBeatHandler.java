package com.netease.jv.cmpp.handler;

import com.netease.jv.cmpp.bean.CMPPMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class HeartBeatHandler extends SimpleChannelInboundHandler<CMPPMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CMPPMessage msg) {
        //接收到心跳包时，在此处打印日志，不再向后分发
        if (msg.getCommand() == CMPPMessage.Command.CMPP_ACTIVE_TEST) {
            ctx.writeAndFlush(
                    new CMPPMessage(12, CMPPMessage.Command.CMPP_ACTIVE_TEST_RESPONSE, msg.getSerial(), new byte[]{}));
            return;
        }
        ctx.fireChannelRead(msg);
    }


}
