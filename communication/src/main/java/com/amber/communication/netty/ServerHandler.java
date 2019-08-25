package com.amber.communication.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * just add 'hello' to msg
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf readBuf = (ByteBuf) msg;
        byte[] inboundMsg = new byte[readBuf.readableBytes()];
        readBuf.readBytes(inboundMsg);
        String req = new String(inboundMsg);
        String resp = "hello "+req;
        ByteBuf respBuf = Unpooled.copiedBuffer(resp.getBytes());
    }
}
