package com.amber.communication.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HelloWorldNettyServer {
    private int port;
    private static ConcurrentMap<Integer, HelloWorldNettyServer> serverCache = new ConcurrentHashMap();

    public static HelloWorldNettyServer create(int port) {
        if (serverCache.get(port) == null) {
            HelloWorldNettyServer newInstance = new HelloWorldNettyServer(port);
            serverCache.putIfAbsent(port, newInstance);
        }
        return serverCache.get(port);
    }

    private HelloWorldNettyServer(int port) {
        this.port = port;
    }

    public void startUp() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ServerHandler());
        try {
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
