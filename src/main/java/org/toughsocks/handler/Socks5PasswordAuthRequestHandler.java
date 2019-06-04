package org.toughsocks.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.component.Socks5Stat;
import org.toughsocks.config.Socks5Config;

@Component
@ChannelHandler.Sharable
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {

	@Autowired
	private Socks5Config socks5Config;

	@Autowired
	private Memarylogger memarylogger;

	@Autowired
	private Socks5Stat socks5Stat;

	private boolean checkUserAndPasswd(String username, String password){
		return true;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) throws Exception {
		if(socks5Config.isDebug())
			memarylogger.info(msg.username(),"用户名密码校验 : " + msg.username() + "," + msg.password(),Memarylogger.SOCKS5);

		if(checkUserAndPasswd(msg.username(), msg.password())) {
			socks5Stat.update(Socks5Stat.AUTH_SUCCESS);
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
			ctx.writeAndFlush(passwordAuthResponse);
		} else {
			socks5Stat.update(Socks5Stat.AUTH_FAIILURE);
			Socks5PasswordAuthResponse passwordAuthResponse = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
			//发送鉴权失败消息，完成后关闭channel
			ctx.writeAndFlush(passwordAuthResponse).addListener(ChannelFutureListener.CLOSE);
		}
	}

}
