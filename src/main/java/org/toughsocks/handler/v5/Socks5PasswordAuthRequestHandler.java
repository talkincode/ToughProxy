package org.toughsocks.handler.v5;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.CoaRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusException;
import org.toughsocks.common.SocksRadiusClient;
import org.toughsocks.component.ConfigService;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.component.SessionCache;
import org.toughsocks.component.SocksStat;
import org.toughsocks.config.Constant;
import org.toughsocks.config.SocksConfig;
import org.toughsocks.entity.SocksAuthResp;

import java.io.IOException;

@Component
@ChannelHandler.Sharable
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> implements Constant {

    @Autowired
    private SocksConfig socksConfig;

    @Autowired
    private Memarylogger memarylogger;

    @Autowired
    private SocksStat socksStat;

    @Autowired
    private SessionCache sessionCache;

    @Autowired
    private ConfigService configService;

    @Autowired
    private Socks5CommandRequestHandler Socks5CommandRequestHandler;

    private SocksAuthResp radiusAuth(String username, String password){
        String nasid = configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_NASID,"toughsocks");
        String secret = configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_AUTH_SECRET,"toughsocks");
        String server = configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_AUTH_SERVER,"127.0.0.1");
        int port = Integer.valueOf(configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_AUTH_PORT,"1812"));
        SocksRadiusClient cli = new SocksRadiusClient(server,port,secret);
        SocksAuthResp resp = null;
        try {
            resp = cli.doAuth(nasid, username, password);
            if(socksConfig.isDebug()){
                memarylogger.print("【socks5】收到 "+username+" Socks RADIUS 认证响应 " + resp.toString());
            }
            return resp;
        } catch (Exception e) {
            return SocksAuthResp.UNKNOW_ERROR;
        }
    }

    private SocksAuthResp localAuth(String username, String password){
        return new SocksAuthResp(0);
    }

    private SocksAuthResp checkUserAndPasswd(String username, String password){
        String authMode = configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_USER_AUTH_MODE);
        if(SOCKS_AUTH_FREE_MODE.equals(authMode)) {
            return SocksAuthResp.SUCCESS;
        }else if(SOCKS_AUTH_LOCAL_MODE.equals(authMode)){
            return localAuth(username,password);
        }else if(SOCKS_AUTH_RADIUS_MODE.equals(authMode)){
            return radiusAuth(username,password);
        }else{
            return SocksAuthResp.UNKNOW_ERROR;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) throws Exception {
        if(socksConfig.isDebug())
            memarylogger.print("【socks5】开始用户"+msg.username()+"认证 : " + msg.username());

        SocksAuthResp resp = checkUserAndPasswd(msg.username(), msg.password());
        if(resp.getCode()==0) {
            socksStat.update(SocksStat.AUTH_SUCCESS);
            Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
            ctx.writeAndFlush(passwordAuthResponse).addListener((ChannelFutureListener) future1 ->{
                if(future1.isSuccess()){
                    if(resp.getUpLimit()>0 && resp.getDownLimit()>0){
                        ctx.pipeline().addLast(new ChannelTrafficShapingHandler(resp.getUpLimit()*1024,resp.getDownLimit()*1024));
                    }
                    if(!sessionCache.isLimitOver(msg.username(),resp.getMaxSession())){
                        ctx.pipeline().addLast(new Socks5CommandRequestDecoder());
                        ctx.pipeline().addLast(Socks5CommandRequestHandler);
                    }else{
                        memarylogger.error(msg.username(), "【socks5】用户并发超过限制", Memarylogger.SOCKS5);
                    }
                }else{
                    ctx.close();
                }
            });
            sessionCache.setUsername(ctx.channel().remoteAddress().toString(),msg.username());
        } else {
            socksStat.update(SocksStat.AUTH_FAIILURE);
            Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
            ctx.writeAndFlush(passwordAuthResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
