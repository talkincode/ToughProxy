package org.toughsocks.handler.v4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughsocks.common.DateTimeUtil;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.component.SessionCache;
import org.toughsocks.component.SocksStat;
import org.toughsocks.component.TicketCache;
import org.toughsocks.config.SocksConfig;
import org.toughsocks.entity.SocksSession;
import org.toughsocks.handler.utils.SocksServerUtils;

import java.net.InetSocketAddress;

@Component
@ChannelHandler.Sharable
public final class Socks4ServerConnectHandler extends SimpleChannelInboundHandler<SocksMessage> {

    @Autowired
    private SocksConfig socksConfig;

    @Autowired
    private Memarylogger memarylogger;

    @Autowired
    private SocksStat socksStat;

    @Autowired
    private SessionCache sessionCache;
    @Autowired
    private TicketCache ticketCache;

    @Override
    public void channelRead0(final ChannelHandlerContext clientChannelContext, final SocksMessage message) throws Exception {
        final Socks4CommandRequest msg = (Socks4CommandRequest) message;
        String targetDesc = msg.type() + "," + msg.dstAddr() + "," + msg.dstPort();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientChannelContext.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //将目标服务器信息转发给客户端
                        ch.pipeline().addLast(new Socks4ServerConnectHandler.Dest2ClientHandler(clientChannelContext));
                    }
                });

        if(socksConfig.isDebug())
            memarylogger.print("【Socks4】1-开始连接目标服务器 : "+targetDesc);

        ChannelFuture future = bootstrap.connect(InetSocketAddress.createUnresolved(msg.dstAddr(), msg.dstPort()));
//			ChannelFuture future = bootstrap.connect(InetSocketAddress.createUnresolved(msg.dstAddr(), msg.dstPort()),clientChannelContext.channel().localAddress());
        future.addListener((ChannelFutureListener) future1 -> {
            if(future1.isSuccess()) {
                if(socksConfig.isDebug())
                    memarylogger.print("【Socks4】2-成功连接目标服务器 : "+targetDesc);

                socksStat.update(SocksStat.CONNECT_SUCCESS);
                clientChannelContext.pipeline().addLast(new Socks4ServerConnectHandler.Client2DestHandler(future1));
                Socks4CommandResponse commandResponse = new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS);
                clientChannelContext.writeAndFlush(commandResponse);

                SocksSession session = new SocksSession();
                session.setType(SocksSession.SOCKS4);
                InetSocketAddress inetSrcaddr = (InetSocketAddress)clientChannelContext.channel().remoteAddress();
                session.setUsername("anonymous");
                session.setSrcAddr(inetSrcaddr.getHostString());
                session.setSrcPort(inetSrcaddr.getPort());
                session.setDstAddr(msg.dstAddr());
                session.setDstPort(msg.dstPort());
                session.setStartTime(DateTimeUtil.getDateTimeString());
                sessionCache.addSession(session);

            } else {
                if(socksConfig.isDebug())
                    memarylogger.print("【Socks4】2-连接目标服务器 "+targetDesc+" 失败");
                socksStat.update(SocksStat.CONNECT_FAILURE);
                SocksServerUtils.closeOnFlush(clientChannelContext.channel());
            }
        });
    }


    private void updateSessionUpBytes(ChannelHandlerContext ctx, long bytes){
        try{
            InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
            String key = addr.getHostString()+ ":"+addr.getPort();
            sessionCache.updateUpBytes(key,bytes);
        }catch (Exception ignore){}
    }

    private void updateSessionDownBytes(ChannelHandlerContext ctx, long bytes){
        try{
            InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
            String key = addr.getHostString()+ ":"+addr.getPort();
            sessionCache.updateDownBytes(key,bytes);
        }catch (Exception ignore){}
    }

    /***
     * 停止连接会话
     * @param ctx
     */
    private void stopSession(ChannelHandlerContext ctx){
        SocksSession sessiion = sessionCache.stopSession((InetSocketAddress) ctx.channel().remoteAddress());
        if(sessiion!=null){
            ticketCache.addTicket(sessiion);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.stopSession(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        this.stopSession(ctx);
    }

    /**
     * 将目标服务器信息转发给客户端
     *
     * @author huchengyi
     *
     */
    private  class Dest2ClientHandler extends ChannelInboundHandlerAdapter {

        private ChannelHandlerContext clientChannelContext;

        public Dest2ClientHandler(ChannelHandlerContext clientChannelContext) {
            this.clientChannelContext = clientChannelContext;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
            ByteBuf message = (ByteBuf) destMsg;
            long bytes = message.readableBytes();
            if(socksConfig.isDebug())
                memarylogger.print("【Socks4】目标服务器-->代理-->客户端传输 ("+bytes+" bytes)");
            clientChannelContext.writeAndFlush(destMsg);
            updateSessionDownBytes(clientChannelContext,bytes);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
            if(socksConfig.isDebug())
                memarylogger.print("【Socks4】断开目标服务器连接");
            if(clientChannelContext.channel().isActive()){
                SocksServerUtils.closeOnFlush(clientChannelContext.channel());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
        }
    }

    /**
     * 将客户端的消息转发给目标服务器端
     * @author huchengyi
     *
     */
    private  class Client2DestHandler extends ChannelInboundHandlerAdapter {

        private ChannelFuture destChannelFuture;
        private SocksSession session;

        public Client2DestHandler(ChannelFuture destChannelFuture) {
            this.destChannelFuture = destChannelFuture;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf message = (ByteBuf) msg;
            long bytes = message.readableBytes();
            if(socksConfig.isDebug())
                memarylogger.print("【Socks4】客户端-->代理-->目标服务器传输 ("+bytes+" bytes)");
            destChannelFuture.channel().writeAndFlush(msg);
            updateSessionUpBytes(ctx,bytes);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if(socksConfig.isDebug())
                memarylogger.print("【Socks4】断开客户端连接");
            destChannelFuture.channel().close();
            if(destChannelFuture.channel().isActive()){
                SocksServerUtils.closeOnFlush(destChannelFuture.channel());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
        }
    }


}