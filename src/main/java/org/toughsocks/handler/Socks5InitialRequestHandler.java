package org.toughsocks.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.component.Socks5Stat;
import org.toughsocks.config.Socks5Config;

@Component
@ChannelHandler.Sharable
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

	@Autowired
	private Memarylogger memarylogger;

	@Autowired
	private Socks5Config socks5Config;

	@Autowired
	private Socks5Stat socks5Stat;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
		if(socks5Config.isDebug())
			memarylogger.info("初始化ss5连接 : " + msg,Memarylogger.SOCKS5);

		if(msg.decoderResult().isFailure()) {
			memarylogger.error("不是ss5协议",Memarylogger.SOCKS5);
			socks5Stat.update(Socks5Stat.NOT_SUPPORT);
			ctx.fireChannelRead(msg);
		} else {
			if(msg.version().equals(SocksVersion.SOCKS5)) {
				if(socks5Config.isAuth()) {
					Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD);
					ctx.writeAndFlush(initialResponse);
				} else {
					Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
					ctx.writeAndFlush(initialResponse);
				}
			}
		}
	}

}
