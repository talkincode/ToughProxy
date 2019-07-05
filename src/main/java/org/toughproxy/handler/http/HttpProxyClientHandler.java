package org.toughproxy.handler.http;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.toughproxy.common.DateTimeUtil;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.component.*;
import org.toughproxy.config.HttpProxyConfig;
import org.toughproxy.entity.SocksSession;
import org.toughproxy.handler.utils.SocksServerUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {

    private HttpProxyConfig httpProxyConfig;
    private Memarylogger memarylogger;
    private AclCache aclCache;
    private AclStat aclStat;
    private ProxyStat proxyStat;
    private SessionCache sessionCache;

    private HttpProxyClientHeader header = new HttpProxyClientHeader();
    private Channel clientChannel;
    private Channel remoteChannel;

    public HttpProxyClientHandler(HttpProxyConfig httpProxyConfig) {
        this.httpProxyConfig = httpProxyConfig;
        this.memarylogger = httpProxyConfig.getMemarylogger();
        this.aclCache = httpProxyConfig.getAclCache();
        this.sessionCache = httpProxyConfig.getLocalSessionCache();
        this.aclStat = httpProxyConfig.getAclStat();
        this.proxyStat = httpProxyConfig.getProxyStat();
    }

    /**
     * 获取连接会话
     * @return
     */
    private SocksSession getSession(){
        return sessionCache.getSession((InetSocketAddress) clientChannel.remoteAddress());
    }

    /**
     * 创建会话对象
     * @return
     */
    private SocksSession createSession(){
        SocksSession session = new SocksSession();
        session.setType(SocksSession.HTTP);
        InetSocketAddress inetSrcaddr = (InetSocketAddress)clientChannel.remoteAddress();
        session.setUsername(sessionCache.getUsername(clientChannel.remoteAddress().toString()));
        session.setSrcAddr(inetSrcaddr.getHostString());
        session.setSrcPort(inetSrcaddr.getPort());
        session.setStartTime(DateTimeUtil.getDateTimeString());
        sessionCache.addSession(session);
        return session;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clientChannel = ctx.channel();
        createSession();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final SocksSession session = getSession();
        if (header.isComplete()) {
            remoteChannel.writeAndFlush(msg); // just forward
            return;
        }

        ByteBuf in = (ByteBuf) msg;
        header.digest(in);

        if (!header.isComplete()) {
            in.release();
            return;
        }

        //更新会话
        session.setDstAddr(header.getHost());
        session.setDstPort(header.getPort());

        memarylogger.info(header.toString(),Memarylogger.HTTP);
        clientChannel.config().setAutoRead(false); // disable AutoRead until remote connection is ready

        // http隧道创建
        if (header.isHttps()) {
            clientChannel.writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes()));
            session.setType(SocksSession.HTTPS);
        }

        // ACL 匹配
        String srcip = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        String destip = InetAddress.getByName(header.getHost()).getHostAddress();
        String destDomain = ValidateUtil.isIP(header.getHost())?null:header.getHost();
        if(aclCache.match(srcip,destip,destDomain)==AclCache.REJECT){
            memarylogger.error("anonymous","ACL Reject for "+srcip + " -> "+destip+"(domain="+destDomain+")",Memarylogger.ACL);
            aclStat.incrementAclReject();
            SocksServerUtils.closeOnFlush(ctx.channel());
            return;
        }else{
            aclStat.incrementAclAccept();
            if(httpProxyConfig.isDebug())
                memarylogger.info("anonymous","ACL Accept for "+srcip + " -> "+destip+"(domain="+destDomain+")",Memarylogger.ACL);
        }

        //连接目标地址
        Bootstrap b = new Bootstrap();
        b.group(clientChannel.eventLoop()).channel(clientChannel.getClass()).handler(new HttpProxyRemoteHandler(clientChannel, httpProxyConfig));
        ChannelFuture f = b.connect(header.getHost(), header.getPort());
        remoteChannel = f.channel();

        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientChannel.config().setAutoRead(true); // connection is ready, enable AutoRead
                if (!header.isHttps()) { // forward header and remaining bytes
                    ByteBuf hmsg = header.getByteBuf();
                    sessionCache.updateUpBytes((InetSocketAddress) clientChannel.remoteAddress(),(long)hmsg.readableBytes());
                    remoteChannel.write(hmsg);
                }
                sessionCache.updateUpBytes((InetSocketAddress) clientChannel.remoteAddress(),(long)in.readableBytes());
                remoteChannel.writeAndFlush(in);
                proxyStat.update(ProxyStat.CONNECT_SUCCESS);
            } else {
                in.release();
                proxyStat.update(ProxyStat.CONNECT_FAILURE);
                SocksServerUtils.closeOnFlush(ctx.channel());
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        flushAndClose(remoteChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        flushAndClose(clientChannel);
    }

    private void flushAndClose(Channel ch) {
        SocksSession session = sessionCache.stopSession((InetSocketAddress) clientChannel.remoteAddress());
        if(session!=null){
            httpProxyConfig.getTicketCache().addTicket(session);
        }
        SocksServerUtils.closeOnFlush(ch);
    }

}