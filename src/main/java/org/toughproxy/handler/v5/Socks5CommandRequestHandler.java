package org.toughproxy.handler.v5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughproxy.component.*;
import org.toughproxy.config.SocksConfig;
import org.toughproxy.handler.utils.SocksServerUtils;
import org.toughproxy.common.DateTimeUtil;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.entity.SocksSession;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

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

    @Autowired
    private AclCache aclCache;

    private final static Map<String,Socks5UdpRelay> relayMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(final ChannelHandlerContext clientChannelContext, DefaultSocks5CommandRequest msg) throws Exception {
        if(msg.type().equals(Socks5CommandType.CONNECT)) {
            String targetDesc = msg.type() + "," + msg.dstAddr() + "," + msg.dstPort();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(clientChannelContext.channel().eventLoop())
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //将目标服务器信息转发给客户端
                    ch.pipeline().addLast(new Dest2ClientHandler(clientChannelContext));
                }
            });
            String username = sessionCache.getUsername(clientChannelContext.channel().remoteAddress().toString());
            // ACL 匹配
            String srcip = ((InetSocketAddress)clientChannelContext.channel().remoteAddress()).getAddress().getHostAddress();
            String destip = InetAddress.getByName(msg.dstAddr()).getHostAddress();
            String destDomain = ValidateUtil.isIP(msg.dstAddr())?null:msg.dstAddr();
            if(aclCache.match(srcip,destip,destDomain)==AclCache.REJECT){
                memarylogger.error(username,"ACL Reject for "+srcip + " -> "+destip+"(domain="+destDomain+")",Memarylogger.ACL);
                socksStat.update(SocksStat.DROP);
                clientChannelContext.close();
                return;
            }else{
                if(socksConfig.isDebug())
                    memarylogger.info(username,"ACL Accept for "+srcip + " -> "+destip+"(domain="+destDomain+")",Memarylogger.ACL);
            }

            if(socksConfig.isDebug())
                memarylogger.print("【Socks5】1-开始连接目标服务器  : "+targetDesc);

            ChannelFuture future = bootstrap.connect(InetSocketAddress.createUnresolved(msg.dstAddr(), msg.dstPort()));
//			ChannelFuture future = bootstrap.connect(InetSocketAddress.createUnresolved(msg.dstAddr(), msg.dstPort()),clientChannelContext.channel().localAddress());
            future.addListener((ChannelFutureListener) future1 -> {
                if(future1.isSuccess()) {
                    if(socksConfig.isDebug())
                        memarylogger.print("【Socks5】2-成功连接目标服务器  : "+targetDesc);

                    socksStat.update(SocksStat.CONNECT_SUCCESS);
                    clientChannelContext.pipeline().addLast(new Client2DestHandler(future1));
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                    clientChannelContext.writeAndFlush(commandResponse);

                    SocksSession session = new SocksSession();
                    session.setType(SocksSession.SOCKS5);
                    InetSocketAddress inetSrcaddr = (InetSocketAddress)clientChannelContext.channel().remoteAddress();
                    session.setUsername(sessionCache.getUsername(clientChannelContext.channel().remoteAddress().toString()));
                    session.setSrcAddr(inetSrcaddr.getHostString());
                    session.setSrcPort(inetSrcaddr.getPort());
                    session.setDstAddr(msg.dstAddr());
                    session.setDstPort(msg.dstPort());
                    session.setStartTime(DateTimeUtil.getDateTimeString());
                    sessionCache.addSession(session);

                } else {
                    if(socksConfig.isDebug())
                        memarylogger.print("【Socks5】2-连接目标服务器 "+targetDesc+" 失败");
                    socksStat.update(SocksStat.CONNECT_FAILURE);
                    SocksServerUtils.closeOnFlush(clientChannelContext.channel());
                }
            });

        }else if(msg.type().equals(Socks5CommandType.UDP_ASSOCIATE)){
            if(socksConfig.isDebug())
                memarylogger.print("【Socks5】0-准备建立UDP中继  : " + msg.type() + ",客户端地址：" + msg.dstAddr() + ",客户端端口：" + msg.dstPort());
            String bindAddr = ((InetSocketAddress)clientChannelContext.channel().localAddress()).getAddress().getHostAddress();
            Socks5UdpRelay udpRelay = new Socks5UdpRelay(msg.dstPort(),memarylogger,socksConfig.isDebug());
            udpRelay.startRelay(clientChannelContext,(ChannelFutureListener) future1 -> {
                if(future1.isSuccess()){
                    if(socksConfig.isDebug())
                        memarylogger.print("【Socks5】UDP 中继创建成功，绑定端口："+udpRelay.getBindPort());

                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS,
                            Socks5AddressType.IPv4,bindAddr,udpRelay.getBindPort());
                    clientChannelContext.writeAndFlush(commandResponse);
                }else{
                    SocksServerUtils.closeOnFlush(clientChannelContext.channel());
                }
            });
            relayMap.put(clientChannelContext.channel().id().asLongText(),udpRelay);
        }else {
            clientChannelContext.fireChannelRead(msg);
        }
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

    /**
     * 关闭 UDP 中继
     * @param ctx
     */
    private void stopUdpRelay(ChannelHandlerContext ctx){
        Socks5UdpRelay relay = relayMap.remove(ctx.channel().id().asLongText());
        if(relay!=null){
            relay.closeRelay();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.stopSession(ctx);
        this.stopUdpRelay(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        this.stopSession(ctx);
        this.stopUdpRelay(ctx);
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
                memarylogger.print("【Socks5】目标服务器-->代理-->客户端传输 ("+bytes+" bytes)");
            clientChannelContext.writeAndFlush(destMsg);
            updateSessionDownBytes(clientChannelContext,bytes);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
            if(socksConfig.isDebug())
                memarylogger.print("【Socks5】断开目标服务器连接");
            clientChannelContext.channel().close();
        }
    }

    /**
     * 将客户端的消息转发给目标服务器端
     *
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
                memarylogger.print("【Socks5】客户端-->代理-->目标服务器传输 ("+bytes+" bytes)");
            destChannelFuture.channel().writeAndFlush(msg);
            updateSessionUpBytes(ctx,bytes);

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if(socksConfig.isDebug())
                memarylogger.print("【Socks5】断开客户端连接");
            destChannelFuture.channel().close();
        }
    }


}
