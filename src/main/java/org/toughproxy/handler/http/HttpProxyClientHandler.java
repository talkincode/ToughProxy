package org.toughproxy.handler.http;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.component.AclCache;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.config.HttpProxyConfig;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {

    private HttpProxyConfig httpProxyConfig;
    private Memarylogger memarylogger;
    private AclCache aclCache;

    private HttpProxyClientHeader header = new HttpProxyClientHeader();
    private Channel clientChannel;
    private Channel remoteChannel;

    public HttpProxyClientHandler(HttpProxyConfig httpProxyConfig) {
        this.httpProxyConfig = httpProxyConfig;
        this.memarylogger = httpProxyConfig.getMemarylogger();
        this.aclCache = httpProxyConfig.getAclCache();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clientChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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

        memarylogger.info(header.toString(),Memarylogger.HTTP);
        clientChannel.config().setAutoRead(false); // disable AutoRead until remote connection is ready

        // http隧道创建
        if (header.isHttps()) {
            clientChannel.writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes()));
        }

        // ACL 匹配
        String srcip = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        String destip = InetAddress.getByName(header.getHost()).getHostAddress();
        String destDomain = ValidateUtil.isIP(header.getHost())?null:header.getHost();
        if(aclCache.match(srcip,destip,destDomain)==AclCache.REJECT){
            memarylogger.error("anonymous","ACL Reject for "+srcip + " -> "+destip+"(domain="+destDomain+")",Memarylogger.ACL);
            httpProxyConfig.getAclStat().incrementAclReject();
            ctx.close();
            return;
        }else{
            httpProxyConfig.getAclStat().incrementAclAccept();
            if(httpProxyConfig.isDebug())
                memarylogger.info("anonymous","ACL Accept for "+srcip + " -> "+destip+"(domain="+destDomain+")",Memarylogger.ACL);
        }

        Bootstrap b = new Bootstrap();
        b.group(clientChannel.eventLoop()).channel(clientChannel.getClass()).handler(new HttpProxyRemoteHandler(clientChannel, httpProxyConfig));
        ChannelFuture f = b.connect(header.getHost(), header.getPort());
        remoteChannel = f.channel();

        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientChannel.config().setAutoRead(true); // connection is ready, enable AutoRead
                if (!header.isHttps()) { // forward header and remaining bytes
                    remoteChannel.write(header.getByteBuf());
                }
                remoteChannel.writeAndFlush(in);
            } else {
                in.release();
                clientChannel.close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        flushAndClose(remoteChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
//        logger.error(id + " shit happens", e);
        flushAndClose(clientChannel);
    }

    private void flushAndClose(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}