package com.amber.communication;

import io.netty.buffer.ByteBuf;

public interface Server {
    /**
     * 启动
     */
    public void startUp();
    /**
     * 从通讯服务中获取客户端信息
     */
    public void onRead();
    /**
     * 关闭
     */
    public void shutdown();
}
