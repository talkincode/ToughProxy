package org.toughproxy.config;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.NettyRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.toughproxy.common.DefaultThreadFactory;
import org.toughproxy.component.AclStat;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.handler.SocksServerInitializer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "org.toughproxy.socks")
public class SocksProxyConfig {

    private int tcpPort;
    private int udpPort;
    private int bossThreads;
    private int workThreads;
    private boolean debug;
    private boolean keepAlive;
    private int backlog;
    private long readLimiit;
    private long writeLimit;
    private long checkInterval;

    @Autowired
    private Memarylogger memarylogger;

    @Autowired
    private AclStat aclStat;

    @Autowired
    private SocksServerInitializer socksServerInitializer;

    private GlobalTrafficShapingHandler trafficHandler;

    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup bossGroup() {
        DefaultThreadFactory factory = new DefaultThreadFactory("SocksPoroxyEvent");
        int pool = Math.max(bossThreads, NettyRuntime.availableProcessors() * 2);
        return Epoll.isAvailable() ? new EpollEventLoopGroup(pool,factory) : new NioEventLoopGroup(pool,factory);
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup workerGroup() {
        DefaultThreadFactory factory = new DefaultThreadFactory("SocksPoroxyEvent");
        int pool = Math.max(workThreads, NettyRuntime.availableProcessors() * 2);
        return Epoll.isAvailable() ? new EpollEventLoopGroup(pool,factory) : new NioEventLoopGroup(pool,factory);
    }

    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpPort() {
        return new InetSocketAddress(tcpPort);
    }

    @Bean(name = "tcpChannelOptions")
    public Map<ChannelOption<?>, Object> tcpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<>();
//        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.SO_BACKLOG, backlog);
        return options;
    }

    @PostConstruct
    public void bootstrap() throws Exception {
        EventLoopGroup _workerGroup = workerGroup();
        EventLoopGroup _bossGroup = bossGroup();
        trafficHandler = new GlobalTrafficShapingHandler(_workerGroup, writeLimit, readLimiit);
        ServerBootstrap b = new ServerBootstrap();
        b.group(_bossGroup, _workerGroup).channel(Epoll.isAvailable() ? EpollServerSocketChannel.class :NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //流量统计
                ch.pipeline().addLast(trafficHandler);
                ch.pipeline().addLast(socksServerInitializer);
            }
        });

        Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
        Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
        for (@SuppressWarnings("rawtypes")ChannelOption option : keySet) {
            b.option(option, tcpChannelOptions.get(option));
        }

        if (Epoll.isAvailable()) {
            b.option(EpollChannelOption.SO_REUSEADDR, true);
            b.option(EpollChannelOption.SO_REUSEPORT, true);
            int cpuNum = NettyRuntime.availableProcessors();
            memarylogger.print(String.format("====== SocksProxyServer listen %s use Epoll and cpu "+cpuNum + "======", tcpPort));
            for (int i = 0; i < cpuNum; i++) {
                ChannelFuture future = b.bind(tcpPort).await();
                if (!future.isSuccess()) {
                    throw new Exception("SocksProxyServer bootstrap bind fail port is " + tcpPort);
                }
            }
        }else{
            memarylogger.print(String.format("====== SocksProxyServer listen %s ======", tcpPort));
            b.bind(tcpPort).await();
        }
    }


    public GlobalTrafficShapingHandler getTrafficHandler() {
        return trafficHandler;
    }

    public void setTrafficHandler(GlobalTrafficShapingHandler trafficHandler) {
        this.trafficHandler = trafficHandler;
    }

    public Memarylogger getMemarylogger() {
        return memarylogger;
    }

    public AclStat getAclStat() {
        return aclStat;
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
