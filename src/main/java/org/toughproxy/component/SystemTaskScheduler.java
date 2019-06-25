package org.toughproxy.component;


import com.google.gson.Gson;
import io.netty.handler.traffic.TrafficCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.toughproxy.config.HttpProxyConfig;
import org.toughproxy.config.SocksProxyConfig;

/**
 * 定时任务设计
 */
@Component
public class SystemTaskScheduler  {


    @Autowired
    private Gson gson;

    @Autowired
    private ThreadPoolTaskExecutor systaskExecutor;

    @Autowired
    private SocksProxyConfig socksProxyConfig;

    @Autowired
    private HttpProxyConfig httpProxyConfig;

    @Autowired
    private Memarylogger logger;

    @Autowired
    private TrafficStat trafficStat;

    @Autowired
    private ProxyStat socks5Stat;

    @Autowired
    private TicketCache ticketCache;

    @Autowired
    private UserCache userCache;

    @Autowired
    private AclCache aclCache;

    @Autowired
    private AclStat aclStat;

    @Autowired
    private SessionCache sessionCache;
    /**
     * 全局流量统计任务
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void updateTrafficStat(){
        systaskExecutor.execute(()->{
            TrafficCounter trafficCounter = socksProxyConfig.getTrafficHandler().trafficCounter();
            TrafficCounter httpTrafficCounter = httpProxyConfig.getTrafficHandler().trafficCounter();
            final long totalRead = trafficCounter.cumulativeReadBytes();
            final long totalWrite = trafficCounter.cumulativeWrittenBytes();
            final long httpTotalRead = httpTrafficCounter.cumulativeReadBytes();
            final long httpTotalWrite = httpTrafficCounter.cumulativeWrittenBytes();
            trafficStat.updateRead(totalRead+httpTotalRead);
            trafficStat.updateWrite(totalWrite+httpTotalWrite);
        });
    }

    /**
     * 消息统计任务
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void updateSocksStat(){
        socks5Stat.runStat();
    }

    /**
     * 消息统计任务
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void updateAclStat(){
        aclStat.runStat();
    }

    /**
     * 同步上网日志
     */
    @Scheduled(fixedDelay = 10 * 1000)
    public void syncTicket() {
        systaskExecutor.execute(()->ticketCache.syncData());
    }

    /**
     * 清理过期会话
     */
    @Scheduled(fixedDelay = 30 * 1000)
    public void clearExpire() {
        systaskExecutor.execute(()->sessionCache.clearExpireSession());
    }


    /**
     * 同步用户缓存
     */
    @Scheduled(fixedDelay = 30 * 1000)
    public void reloadUserCache() {
        systaskExecutor.execute(()->userCache.updateUserCache());
    }


    /**
     * 同步ACL缓存
     */
    @Scheduled(fixedDelay = 3600 * 1000, initialDelay = 1000)
    public void reloadAclCache() {
        systaskExecutor.execute(()->aclCache.updateAclCache());
    }



}
