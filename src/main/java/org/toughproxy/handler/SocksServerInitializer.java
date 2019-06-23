package org.toughproxy.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class SocksServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private SocksServerHandler socksServerHandler;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        ch.pipeline().addLast(new SocksPortUnificationServerHandler());
        ch.pipeline().addLast(socksServerHandler);
    }
}