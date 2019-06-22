package org.toughsocks.handler.v5;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.GenericFutureListener;
import org.toughsocks.component.Memarylogger;
import java.net.InetSocketAddress;

public class Socks5UdpRelay {

    private EventLoopGroup group;

    private ChannelFuture channelFuture;

    private Memarylogger logger;

    private int srcPort;

    private int bindPort;

    private boolean debug;

    private ChannelHandlerContext clientChannelContext;

    public int getBindPort() {
        return bindPort;
    }

    public Socks5UdpRelay(int srcPort, Memarylogger logger, boolean debug) {
        this.debug = debug;
        this.srcPort = srcPort;
        this.logger = logger;
    }

    /**
     * 启动UDP中继
     * @param clientChannelContext
     * @param listen
     */
    public void startRelay(ChannelHandlerContext clientChannelContext, GenericFutureListener listen) {
        this.clientChannelContext = clientChannelContext;
        this.group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpRelayHandler());
            channelFuture = bootstrap.bind("0.0.0.0",0).sync();
            this.bindPort = ((InetSocketAddress) channelFuture.channel().localAddress()).getPort();
            channelFuture.addListener(listen);
        } catch (InterruptedException e) {
            logger.error("init Socks5UdpRelay error",e,Memarylogger.SOCKS5);
            clientChannelContext.fireChannelInactive();
        }
    }

    /**
     * 关闭UDP中继
     */
    public void closeRelay() {
        try {
            if(channelFuture!=null){
                try {
                    if (debug)
                        logger.print("关闭UDP中继(BIND:)" + channelFuture.channel().localAddress().toString());
                    channelFuture.channel().closeFuture();
                } catch (Exception ignore) {
                }
            }
            try {
                if (debug)
                    logger.print("关闭客户端连接：" + clientChannelContext.channel().remoteAddress().toString());
                clientChannelContext.close();
            } catch (Exception ignore) {
            }
        }finally {
            group.shutdownGracefully();
        }
    }

    /**
     * UDP 中继处理
     */
    class UdpRelayHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            if(srcPort>0 && msg.sender().getPort() != srcPort){
                logger.error("客户端UDP数据来源端口不匹配, 关闭UDP代理中继服务",  Memarylogger.SOCKS5);
                ctx.channel().close();
                clientChannelContext.fireChannelInactive();
            }else{
                Socks5UdpMessage umsg  = new Socks5UdpMessage(msg.content());
                Socks5UdpSender.asyncSendData(umsg.getData(), umsg.getDstAddr(), umsg.getDstPort(), 3000, new Socks5UdpSender.SendResultHandler() {
                    @Override
                    public void onResp(byte[] data) {
                        Socks5UdpMessage resp = new Socks5UdpMessage();
                        resp.setRsv(umsg.getRsv());
                        resp.setFrag(umsg.getFrag());
                        resp.setAtype(umsg.getAtype());
                        resp.setDstAddr(umsg.getDstAddr());
                        resp.setDstPort(umsg.getDstPort());
                        resp.setData(data);
                        ctx.writeAndFlush(new DatagramPacket(resp.encode(),msg.sender()));
                    }
                    @Override
                    public void onError(Exception ioex) {
                        logger.error("Socks5UdpSender.asyncSendData error", ioex, Memarylogger.SOCKS5);
                        clientChannelContext.fireChannelInactive();
                    }
                });
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            clientChannelContext.fireChannelInactive();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            clientChannelContext.fireChannelInactive();
        }
    }

    public static void main(String args[]){

    }

}
