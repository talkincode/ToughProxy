package org.toughproxy.handler;

import io.netty.channel.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughproxy.config.HttpProxyConfig;
import org.toughproxy.handler.http.HttpProxyClientHandler;

@Component
@ChannelHandler.Sharable
public class HttpProxyInitializer extends ChannelInitializer<Channel> {

    @Autowired
    private HttpProxyConfig httpProxyConfig;

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        channel.pipeline().addLast(new HttpProxyClientHandler(httpProxyConfig));
    }

}
