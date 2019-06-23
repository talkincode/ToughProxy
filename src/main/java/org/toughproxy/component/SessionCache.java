package org.toughproxy.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughproxy.common.DateTimeUtil;
import org.toughproxy.common.PageResult;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.entity.SocksSession;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理会话缓存， 记录更新连接信息
 */
@Component
public class SessionCache {

    private final static Map<String, SocksSession> cacheData = new ConcurrentHashMap<>();
    private final static Map<String, String> usernameCache = new ConcurrentHashMap<>();

    @Autowired
    private Memarylogger logger;

    public void setUsername(String key, String username){
        usernameCache.put(key,username);
    }

    public String getUsername(String key){
        return usernameCache.get(key);
    }

    public void removeUsername(String key){
        usernameCache.remove(key);
    }

    public void addSession(SocksSession session){
        cacheData.put(session.getKey(),session);
    }

    public SocksSession remove(String key){
        return cacheData.remove(key);
    }

    public void updateUpBytes(String key,Long bytes){
        SocksSession session = cacheData.get(key);
        session.setUpBytes(session.getUpBytes()+bytes);
    }

    public void updateDownBytes(String key,Long bytes){
        SocksSession session = cacheData.get(key);
        session.setDownBytes(session.getDownBytes()+bytes);
    }

    public SocksSession stopSession(String key){
        SocksSession session = cacheData.remove(key);
        if(session!=null)
            session.setEndTime(DateTimeUtil.getDateTimeString());
        return session;
    }

    public SocksSession stopSession(InetSocketAddress addr){
        String key = addr.getHostString()+ ":"+addr.getPort();
        SocksSession session = cacheData.remove(key);
        if(session!=null)
            session.setEndTime(DateTimeUtil.getDateTimeString());
        return session;
    }

    /** 查询上网帐号并发数 是否超过限制， 超过预设即返回， 避免多次循环*/
    public boolean isLimitOver(String userName, int limit)
    {
        if(limit==0){
            return false;
        }
        int onlineNum = 0;
        for (SocksSession session : cacheData.values()) {
            if (userName.equals(session.getUsername())){
                onlineNum++;
            }
            if (onlineNum >= limit){
                return true;
            }
        }
        return onlineNum >= limit;
    }


    /**
     * 删除过期无效的会话
     */
    public void clearExpireSession(){
        String ctime = DateTimeUtil.getDateTimeString();
        cacheData.values().removeIf(x-> x.getUpBytes() == 0 && x.getDownBytes() == 0 && DateTimeUtil.compareSecond(ctime,x.getStartTime())>60);
    }


    private boolean filterSession(SocksSession online, String username,String srcAddr,Integer srcPort,String dstAddr,Integer dstPort) {

        if (ValidateUtil.isNotEmpty(username) && !online.getUsername().contains(username))
            return false;

        if (ValidateUtil.isNotEmpty(srcAddr) && !srcAddr.equals(online.getSrcAddr()))
            return false;

        if (srcPort!=null&& !srcPort.equals(online.getSrcPort()))
            return false;

        if (ValidateUtil.isNotEmpty(dstAddr) && !dstAddr.equals(online.getDstAddr()))
            return false;

        if (dstPort!=null&& !dstPort.equals(online.getDstPort()))
            return false;

        return true;
    }

    /**
     * 查询会会话列表
     * @param pos
     * @param count
     * @param username
     * @param srcAddr
     * @param srcPort
     * @param dstAddr
     * @param dstPort
     * @param sort
     * @return
     */
    public PageResult<SocksSession> querySessionPage(int pos, int count, String username, String srcAddr, Integer srcPort, String dstAddr,
                                                     Integer dstPort, String sort){
        int total = 0;
        int start = pos+1;
        int end = pos +  count ;
        List<SocksSession> copyList = new ArrayList<>(cacheData.values());
        List<SocksSession> onlineList = new ArrayList<>();
        for (SocksSession session : copyList) {
            if (!this.filterSession(session, username, srcAddr,srcPort, dstAddr, dstPort)) {
                continue;
            }
            total++;
            if (total >= start && total <= end) {
                try {
                    onlineList.add(session.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return new PageResult<>(pos, total, onlineList);
    }



}
