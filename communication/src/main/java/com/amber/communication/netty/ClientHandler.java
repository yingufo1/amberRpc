package com.amber.communication.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

public class ClientHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext cxt,Object msg) throws Exception {
        ByteBuf buf = (ByteBuf)msg;
        byte[] req = new byte [buf.readableBytes()];
        buf.readBytes(req);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }
}
