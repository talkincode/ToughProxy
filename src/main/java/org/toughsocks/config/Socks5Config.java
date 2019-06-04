package org.toughsocks.config;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.handler.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "org.toughsocks.ss5")
public class Socks5Config {

    private int tcpPort;
    private int udpPort;
    private int bossThreads;
    private int workThreads;
    private boolean debug;
    private boolean auth;
    private boolean keepAlive;
    private int backlog;
    private long readLimiit;
    private long writeLimit;
    private long checkInterval;

    private ChannelFuture serverChannelFuture;

    @Autowired
    private Memarylogger memarylogger;

    @Autowired
    private Socks5InitialRequestHandler socks5InitialRequestHandler;

    @Autowired
    private Socks5PasswordAuthRequestHandler socks5PasswordAuthRequestHandler;

    @Autowired
    private Socks5CommandRequestHandler socks5CommandRequestHandler;

    private GlobalTrafficShapingHandler trafficHandler;

    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossThreads);
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workThreads);
    }

    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpPort() {
        return new InetSocketAddress(tcpPort);
    }

    @Bean(name = "tcpChannelOptions")
    public Map<ChannelOption<?>, Object> tcpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<>();
        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.SO_BACKLOG, backlog);
        return options;
    }

    @PostConstruct
    public void bootstrap() throws InterruptedException {
        NioEventLoopGroup _workerGroup = workerGroup();
        NioEventLoopGroup _bossGroup = bossGroup();
        trafficHandler = new GlobalTrafficShapingHandler(_workerGroup, writeLimit, readLimiit);
        ServerBootstrap b = new ServerBootstrap();
        b.group(_bossGroup, _workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //流量统计
                ch.pipeline().addLast(trafficHandler);
                //channel超时处理
                ch.pipeline().addLast(new IdleStateHandler(3, 30, 0));
                ch.pipeline().addLast(new ProxyIdleHandler());
                //netty日志
                ch.pipeline().addLast(new LoggingHandler());
                //Socks5MessagByteBuf
                ch.pipeline().addLast(Socks5ServerEncoder.DEFAULT);
                //sock5 init
                ch.pipeline().addLast(new Socks5InitialRequestDecoder());
                //sock5 init
                ch.pipeline().addLast(socks5InitialRequestHandler);
                if(isAuth()) {
                    //socks auth
                    ch.pipeline().addLast(new Socks5PasswordAuthRequestDecoder());
                    //socks auth
                    ch.pipeline().addLast(socks5PasswordAuthRequestHandler);
                }
                //socks connection
                ch.pipeline().addLast(new Socks5CommandRequestDecoder());
                //Socks connection
                ch.pipeline().addLast(socks5CommandRequestHandler);
            }
        });
        Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
        Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
        for (@SuppressWarnings("rawtypes")ChannelOption option : keySet) {
            b.option(option, tcpChannelOptions.get(option));
        }
        memarylogger.print(String.format("====== Socks5Server listen %s ======", tcpPort));
        serverChannelFuture = b.bind(tcpPort).sync();
    }

    @PreDestroy
    public void stop() throws Exception {
        serverChannelFuture.channel().closeFuture();
    }

    public GlobalTrafficShapingHandler getTrafficHandler() {
        return trafficHandler;
    }

    public void setTrafficHandler(GlobalTrafficShapingHandler trafficHandler) {
        this.trafficHandler = trafficHandler;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public int getBossThreads() {
        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public int getBacklog() {
        return backlog;
    }

    public long getReadLimiit() {
        return readLimiit;
    }

    public void setReadLimiit(long readLimiit) {
        this.readLimiit = readLimiit;
    }

    public long getWriteLimit() {
        return writeLimit;
    }

    public void setWriteLimit(long writeLimit) {
        this.writeLimit = writeLimit;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
