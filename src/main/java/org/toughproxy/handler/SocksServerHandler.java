package org.toughproxy.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.component.SocksStat;
import org.toughproxy.handler.v4.Socks4ServerConnectHandler;
import org.toughproxy.config.Constant;
import org.toughproxy.handler.utils.SocksServerUtils;
import org.toughproxy.handler.v5.Socks5InitialRequestHandler;

@Component
@ChannelHandler.Sharable
public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> implements Constant {

    @Autowired
    private Memarylogger memarylogger;


    @Autowired
    private SocksStat socksStat;

    @Autowired
    private Socks5InitialRequestHandler socks5InitialRequestHandler;

    @Autowired
    private Socks4ServerConnectHandler socks4ServerConnectHandler;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SocksMessage socksRequest) throws Exception {


        if(socksRequest.decoderResult().isFailure()) {
            memarylogger.error("不是 Socks 协议",Memarylogger.ERROR);
            socksStat.update(SocksStat.NOT_SUPPORT);
            ctx.fireChannelRead(socksRequest);
            return;
        }

        switch (socksRequest.version()) {
            case SOCKS4a:
                Socks4CommandRequest socksV4CmdRequest = (Socks4CommandRequest) socksRequest;
                if (socksV4CmdRequest.type() == Socks4CommandType.CONNECT) {
                    ctx.pipeline().addLast(socks4ServerConnectHandler);
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksRequest);
                } else {
                    ctx.close();
                }
                break;
            case SOCKS5:
                if (socksRequest instanceof Socks5InitialRequest) {
                    ctx.pipeline().addLast(socks5InitialRequestHandler);
//                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksRequest);
                }else {
                    ctx.close();
                }
                break;
            case UNKNOWN:
                memarylogger.error("不是 Socks 协议",Memarylogger.ERROR);
                socksStat.update(SocksStat.NOT_SUPPORT);
                ctx.close();
                break;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        throwable.printStackTrace();
        SocksServerUtils.closeOnFlush(ctx.channel());
    }
}