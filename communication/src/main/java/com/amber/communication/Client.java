package com.amber.communication;

public interface Client {
    /**
     * 启动连接
     */
    public void connect(String host,int port);
    /**
     * 发送请求,返回响应
     * @param request
     * @return response from remote
     */
    public String send(String request);

    /**
     * 断开连接
     */
    public void disConnect();

}
