package org.toughproxy.handler.http;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.component.SessionCache;
import org.toughproxy.config.HttpProxyConfig;

import java.net.InetSocketAddress;


public class HttpProxyRemoteHandler extends ChannelInboundHandlerAdapter {

    private HttpProxyConfig httpProxyConfig;
    private Memarylogger memarylogger;
    private SessionCache sessionCache;

    private Channel clientChannel;
    private Channel remoteChannel;

    public HttpProxyRemoteHandler(Channel clientChannel,HttpProxyConfig httpProxyConfig) {
        this.clientChannel = clientChannel;
        this.httpProxyConfig = httpProxyConfig;
        this.memarylogger = httpProxyConfig.getMemarylogger();
        this.sessionCache = httpProxyConfig.getSessionCache();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.remoteChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf message = (ByteBuf) msg;
        long bytes = message.readableBytes();
        clientChannel.writeAndFlush(msg); // just forward
        sessionCache.updateDownBytes((InetSocketAddress) ctx.channel().remoteAddress(),bytes);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        flushAndClose(clientChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        memarylogger.error("Error ", e, Memarylogger.HTTP);
        flushAndClose(remoteChannel);
    }

    private void flushAndClose(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}