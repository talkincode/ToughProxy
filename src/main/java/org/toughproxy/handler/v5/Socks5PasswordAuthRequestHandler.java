package org.toughproxy.handler.v5;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughproxy.common.ValidateCache;
import org.toughproxy.component.*;
import org.toughproxy.config.Constant;
import org.toughproxy.config.SocksProxyConfig;
import org.toughproxy.common.DateTimeUtil;
import org.toughproxy.common.SocksRadiusClient;
import org.toughproxy.entity.SocksAuthResp;
import org.toughproxy.entity.User;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> implements Constant {

    @Autowired
    private SocksProxyConfig socksProxyConfig;

    @Autowired
    private Memarylogger memarylogger;

    @Autowired
    private ProxyStat proxyStat;

    @Autowired
    private SessionCache sessionCache;

    @Autowired
    private UserCache userCache;

    @Autowired
    private ConfigService configService;

    @Autowired
    private Socks5CommandRequestHandler Socks5CommandRequestHandler;

    private final static Map<String, ValidateCache> validateMap = new ConcurrentHashMap<>();

    private ValidateCache getUserValidateCache(String username, int limit){
        ValidateCache v = validateMap.get(username);
        if(v.getMaxTimes()!=limit){
            validateMap.remove(username);
            v = null;
        }
        if(v == null){
            v = new ValidateCache(3 * 60 *1000,limit);
        }
        validateMap.put(username,v);
        return v;
    }

    /**
     * RADIUS 认证
     * @param username
     * @param password
     * @return
     */
    private SocksAuthResp radiusAuth(String username, String password){
        String nasid = configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_NASID,"toughproxy");
        String secret = configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_AUTH_SECRET,"toughproxy");
        String server = configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_AUTH_SERVER,"127.0.0.1");
        int port = Integer.valueOf(configService.getStringValue(SYSTEM_MODULE,SYSTEM_SOCKS_RADIUS_AUTH_PORT,"1812"));
        SocksRadiusClient cli = new SocksRadiusClient(server,port,secret);
        SocksAuthResp resp = null;
        try {
            resp = cli.doAuth(nasid, username, password);
            if(socksProxyConfig.isDebug()){
                memarylogger.info("【socks5】收到 "+username+" Socks RADIUS 认证响应 " + resp.toString(),Memarylogger.SOCKS5);
            }
            return resp;
        } catch (Exception e) {
            return SocksAuthResp.UNKNOW_ERROR;
        }
    }

    /**
     * 本地认证
     * @param username
     * @param password
     * @return
     */
    private SocksAuthResp localAuth(String username, String password){
        User user = userCache.findGUser(username);
        if(user==null){
            memarylogger.error( username,"【socks5】本地认证失败 "+SocksAuthResp.USER_NOT_EXISTS.getMessage(), Memarylogger.SOCKS5);
            return SocksAuthResp.USER_NOT_EXISTS;
        }
        if(!user.getPassword().equals(password)){
            memarylogger.error( username,"【socks5】本地认证失败 "+SocksAuthResp.USER_PASSWD_ERROR.getMessage(), Memarylogger.SOCKS5);
            return SocksAuthResp.USER_PASSWD_ERROR;
        }
        if(user.getStatus()!=1){
            memarylogger.error( username,"【socks5】本地认证失败 "+SocksAuthResp.USER_STATUS_ERROR.getMessage(), Memarylogger.SOCKS5);
            return SocksAuthResp.USER_STATUS_ERROR;
        }
        if(user.getExpireTime().getTime() < DateTimeUtil.nowTimestamp().getTime()){
            memarylogger.error( username,"【socks5】本地认证失败 "+SocksAuthResp.USER_EXPIRE.getMessage(), Memarylogger.SOCKS5);
            return SocksAuthResp.USER_EXPIRE;
        }
        if(user.getGroupPolicy()==1&&user.getGroupStatus()==0){
            memarylogger.error( username,"【socks5】本地认证失败 "+SocksAuthResp.USER_GROUP_STATUS_ERROR.getMessage(), Memarylogger.SOCKS5);
            return SocksAuthResp.USER_GROUP_STATUS_ERROR;
        }

        SocksAuthResp resp =  new SocksAuthResp(0);
        if(user.getGroupPolicy()==1){
            resp.setMaxSession(user.getGroupMaxSession());
            resp.setMaxClient(user.getGroupMaxClient());
            resp.setUpLimit(user.getGroupUpLimit());
            resp.setDownLimit(user.getGroupDownLimit());
        }else{
            resp.setMaxSession(user.getMaxSession());
            resp.setMaxClient(user.getMaxClient());
            resp.setUpLimit(user.getUpLimit());
            resp.setDownLimit(user.getDownLimit());
        }
        if(socksProxyConfig.isDebug()){
            memarylogger.info(username,"【socks5】收到 "+username+" Socks 本地认证响应 " + resp.toString(),Memarylogger.SOCKS5);
        }
        return resp;
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
        if(socksProxyConfig.isDebug())
            memarylogger.info(msg.username(),"【socks5】开始用户"+msg.username()+"认证 : " + msg.username(),Memarylogger.SOCKS5);

        SocksAuthResp resp = checkUserAndPasswd(msg.username(), msg.password());
        if(resp.getCode()==0) {
            proxyStat.update(ProxyStat.AUTH_SUCCESS);
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
                        memarylogger.error(msg.username(), "【socks5】用户并发超过限制:"+resp.getMaxSession(), Memarylogger.SOCKS5);
                    }

                    if(resp.getMaxClient()>0){
                        ValidateCache vc = getUserValidateCache(msg.username(), resp.getMaxClient());
                        String srcaddr = ((InetSocketAddress)ctx.channel().remoteAddress()).getHostString();
                        vc.incr(srcaddr);
                        if(vc.isOver(srcaddr)){
                            memarylogger.error(msg.username(), "【socks5】用户3分钟内客户端数量超过限制:"+resp.getMaxClient(), Memarylogger.SOCKS5);
                        }
                    }
                }else{
                    ctx.close();
                }
            });
            sessionCache.setUsername(ctx.channel().remoteAddress().toString(),msg.username());
        } else {
            proxyStat.update(ProxyStat.AUTH_FAIILURE);
            Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
            ctx.writeAndFlush(passwordAuthResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
