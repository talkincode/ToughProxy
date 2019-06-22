package org.toughsocks.handler.v5;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughsocks.component.ConfigService;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.component.SocksStat;
import org.toughsocks.config.Constant;
import org.toughsocks.config.SocksConfig;

@Component
@ChannelHandler.Sharable
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> implements Constant {

    @Autowired
    private Memarylogger memarylogger;

    @Autowired
    private SocksConfig socksConfig;

    @Autowired
    private SocksStat socks5Stat;

    @Autowired
    private ConfigService configService;

    @Autowired
    private Socks5PasswordAuthRequestHandler socks5PasswordAuthRequestHandler;

    @Autowired
    private Socks5CommandRequestHandler Socks5CommandRequestHandler;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
        if(socksConfig.isDebug())
            memarylogger.print("【socks5】初始化 socks5 连接 : " + msg);

        if(!SOCKS_AUTH_FREE_MODE.equals(configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_USER_AUTH_MODE))) {
            Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD);
            ctx.writeAndFlush(initialResponse).addListener((ChannelFutureListener) future1 ->{
                if(future1.isSuccess()){
                    ctx.pipeline().addLast(new Socks5PasswordAuthRequestDecoder());
                    ctx.pipeline().addLast(socks5PasswordAuthRequestHandler);
                }else{
                    ctx.close();
                }
            });
        } else {
            Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
            ctx.writeAndFlush(initialResponse).addListener((ChannelFutureListener) future1 ->{
                if(future1.isSuccess()){
                    ctx.pipeline().addLast(new Socks5CommandRequestDecoder());
                    ctx.pipeline().addLast(Socks5CommandRequestHandler);
                }else{
                    ctx.close();
                }
            });
        }
    }

}
