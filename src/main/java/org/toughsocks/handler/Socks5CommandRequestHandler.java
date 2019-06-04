package org.toughsocks.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.component.Socks5Stat;
import org.toughsocks.config.Socks5Config;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

	@Autowired
	private Socks5Config socks5Config;

	@Autowired
	private Memarylogger memarylogger;

	@Autowired
	private NioEventLoopGroup bossGroup;

	@Autowired
	private Socks5Stat socks5Stat;

	private final static Map<String,Socks5UdpRelay> relayMap = new ConcurrentHashMap<>();

	@Override
	protected void channelRead0(final ChannelHandlerContext clientChannelContext, DefaultSocks5CommandRequest msg) throws Exception {
		if(msg.type().equals(Socks5CommandType.CONNECT)) {
			if(socks5Config.isDebug())
				memarylogger.print("0-准备连接目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());

			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(bossGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					//ch.pipeline().addLast(new LoggingHandler());//in out
					//将目标服务器信息转发给客户端
					ch.pipeline().addLast(new Dest2ClientHandler(clientChannelContext));
				}
			});

			if(socks5Config.isDebug())
				memarylogger.print("1-开始连接目标服务器");

			ChannelFuture future = bootstrap.connect(InetSocketAddress.createUnresolved(msg.dstAddr(), msg.dstPort()));
//			ChannelFuture future = bootstrap.connect(InetSocketAddress.createUnresolved(msg.dstAddr(), msg.dstPort()),clientChannelContext.channel().localAddress());
			future.addListener((ChannelFutureListener) future1 -> {
				if(future1.isSuccess()) {
					if(socks5Config.isDebug())
						memarylogger.print("2-成功连接目标服务器");

					socks5Stat.update(Socks5Stat.CONNECT_SUCCESS);
					clientChannelContext.pipeline().addLast(new Client2DestHandler(future1));
					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
					clientChannelContext.writeAndFlush(commandResponse);
				} else {
					if(socks5Config.isDebug())
						memarylogger.print("2-连接目标服务器失败");

					socks5Stat.update(Socks5Stat.CONNECT_FAILURE);
					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
					clientChannelContext.writeAndFlush(commandResponse);
				}
			});

		}else if(msg.type().equals(Socks5CommandType.UDP_ASSOCIATE)){
			if(socks5Config.isDebug())
				memarylogger.print("0-准备建立UDP中继  : " + msg.type() + ",客户端地址：" + msg.dstAddr() + ",客户端端口：" + msg.dstPort());
			String bindAddr = ((InetSocketAddress)clientChannelContext.channel().localAddress()).getAddress().getHostAddress();
			Socks5UdpRelay udpRelay = new Socks5UdpRelay(msg.dstPort(),memarylogger,socks5Config.isDebug());
			udpRelay.startRelay(clientChannelContext,(ChannelFutureListener) future1 -> {
				if(future1.isSuccess()){
					if(socks5Config.isDebug())
						memarylogger.print("UDP 中继创建成功，绑定端口："+udpRelay.getBindPort());

					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS,
							Socks5AddressType.IPv4,bindAddr,udpRelay.getBindPort());
					clientChannelContext.writeAndFlush(commandResponse);
				}else{
					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
					clientChannelContext.writeAndFlush(commandResponse);
				}
			});
			relayMap.put(clientChannelContext.channel().id().asLongText(),udpRelay);
		}else {
			clientChannelContext.fireChannelRead(msg);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		Socks5UdpRelay relay = relayMap.remove(ctx.channel().id().asLongText());
		if(relay!=null){
			relay.closeRelay();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		Socks5UdpRelay relay = relayMap.remove(ctx.channel().id().asLongText());
		if(relay!=null){
			relay.closeRelay();
		}
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
			if(socks5Config.isDebug())
				memarylogger.print("将目标服务器信息转发给客户端");
			clientChannelContext.writeAndFlush(destMsg);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
			if(socks5Config.isDebug())
				memarylogger.print("目标服务器断开连接");
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
		
		public Client2DestHandler(ChannelFuture destChannelFuture) {
			this.destChannelFuture = destChannelFuture;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if(socks5Config.isDebug())
				memarylogger.print("将客户端的消息转发给目标服务器端");
			destChannelFuture.channel().writeAndFlush(msg);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			if(socks5Config.isDebug())
				memarylogger.print("客户端断开连接");
			destChannelFuture.channel().close();
		}
	}


}
